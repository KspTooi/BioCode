package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.model.attach.AttachPo;
import com.ksptool.bio.biz.core.model.attachpool.AttachPoolPo;
import com.ksptool.bio.biz.core.model.attachpool.dto.GetAttachListDto;
import com.ksptool.bio.biz.core.model.attachpool.vo.GetAttachListVo;
import com.ksptool.bio.biz.core.model.attachpool.vo.GetLatestScanRecordVo;
import com.ksptool.bio.biz.core.repository.AttachPoolRepository;
import com.ksptool.bio.biz.core.repository.AttachRepository;
import com.ksptool.bio.commons.config.AttachConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Slf4j
@Service
public class AttachPoolService {

    //扫描锁
    private final ReentrantLock scanLock = new ReentrantLock();

    @Autowired
    private AttachPoolRepository attachPoolRepository;

    @Autowired
    private AttachConfig attachConfig;

    @Autowired
    private AttachRepository attachRepository;

    /**
     * 查询最新的附件池扫描记录
     *
     * @return 最新的附件池扫描记录
     */
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public GetLatestScanRecordVo getLatestScanRecord() {
        AttachPoolPo attachPool = attachPoolRepository.getLatestScanRecord();
        if (attachPool == null) {
            return null;
        }
        return as(attachPool, GetLatestScanRecordVo.class);
    }

    /**
     * 分页查询附件列表
     *
     * @param dto 查询条件
     * @return 附件列表
     */
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public PageResult<GetAttachListVo> getAttachList(GetAttachListDto dto) {
        AttachPo query = new AttachPo();
        assign(dto, query);

        Page<AttachPo> page = attachRepository.getAttachList(query, dto.getIndexFilter(), dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAttachListVo> vos = as(page.getContent(), GetAttachListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 快速扫描附件池。统计目录文件数/总字节与磁盘占用，不校验已索引附件是否仍存在于磁盘。
     */
    @Transactional(rollbackFor = Exception.class)
    public void quickScanAttachPool() throws BizException {

        if (!scanLock.tryLock()) {
            throw new BizException("附件池正在扫描中，请稍后再试");
        }

        try {
            AttachPoolPo latest = attachPoolRepository.getLatestScanRecord();

            if (latest != null && latest.getScanStatus() == 0) {
                log.warn("检测到前次扫描未完成(scanStatus=0, id={})，锁已释放，判断为前次扫描异常中止，将创建新的扫描记录", latest.getId());
            }

            String osName = System.getProperty("os.name").toLowerCase();
            Path poolRoot = null;
            if (osName.contains("win")) {
                poolRoot = Paths.get(attachConfig.getLocalWindowsPath());
            }
            if (poolRoot == null && osName.contains("linux")) {
                poolRoot = Paths.get(attachConfig.getLocalLinuxPath());
            }
            if (poolRoot == null) {
                throw new BizException("不支持的操作系统: " + osName);
            }

            AttachPoolPo insertPo = new AttachPoolPo();
            insertPo.setPoolPath(poolRoot.toString());
            insertPo.setPoolCapacityBytes(0L);
            insertPo.setPoolUsageBytes(0L);
            insertPo.setPoolAttachesBytes(0L);
            insertPo.setIndexedCount(0);
            insertPo.setDriftCount(0);
            insertPo.setScanStartTime(LocalDateTime.now());
            insertPo.setScanStatus(0);
            attachPoolRepository.save(insertPo);
            long recordId = insertPo.getId();

            if (!Files.exists(poolRoot)) {
                log.warn("附件池目录不存在，将创建空目录: {}", poolRoot);
                Files.createDirectories(poolRoot);
            }

            AtomicLong fileCount = new AtomicLong(0);
            AtomicLong totalBytes = new AtomicLong(0);
            Files.walkFileTree(poolRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        fileCount.incrementAndGet();
                        totalBytes.addAndGet(attrs.size());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.warn("无法访问文件: {} - {}", file, exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });

            long indexedCount = attachRepository.countValidAttaches();
            long driftCount = fileCount.get() - indexedCount;
            if (driftCount < 0) {
                driftCount = 0;
            }

            long poolCapacityBytes = poolRoot.toFile().getTotalSpace();
            long poolAvailableBytes = poolRoot.toFile().getUsableSpace();
            long poolUsageBytes = poolCapacityBytes - poolAvailableBytes;
            if (poolUsageBytes < 0) {
                poolUsageBytes = 0;
            }

            AttachPoolPo updatePo = attachPoolRepository.findById(recordId)
                    .orElseThrow(() -> new BizException("扫描记录不存在"));
            updatePo.setPoolCapacityBytes(poolCapacityBytes);
            updatePo.setPoolUsageBytes(poolUsageBytes);
            updatePo.setPoolAttachesBytes(totalBytes.get());
            updatePo.setIndexedCount((int) indexedCount);
            updatePo.setDriftCount((int) driftCount);
            updatePo.setScanEndTime(LocalDateTime.now());
            updatePo.setScanStatus(1);
            attachPoolRepository.save(updatePo);

            log.info("附件池快速扫描完成。文件总数:{} 已索引:{} 游离:{} 附件字节:{} 附件池已用:{} 附件池容量:{}",
                    fileCount.get(), indexedCount, driftCount, totalBytes.get(), poolUsageBytes, poolCapacityBytes);

        } catch (IOException e) {
            log.error("附件池快速扫描异常", e);
            throw new BizException("附件池扫描失败: " + e.getMessage());
        } finally {
            scanLock.unlock();
        }
    }

    /**
     * 深度扫描附件池。校验已索引附件是否仍存在于磁盘。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deepScanAttachPool() throws BizException {
        throw new BizException("深度扫描尚未开放");
    }
}
