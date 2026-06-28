package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.model.attach.AttachPo;
import com.ksptool.bio.biz.core.model.attachpool.AttachPoolPo;
import com.ksptool.bio.biz.core.model.attachpool.dto.GetAttachListDto;
import com.ksptool.bio.biz.core.model.attachpool.vo.GetAttachListVo;
import com.ksptool.bio.biz.core.model.attachpool.vo.GetLatestScanRecordVo;
import com.ksptool.bio.biz.core.model.attachpool.vo.GetRebuildIndexStatusVo;
import com.ksptool.bio.biz.core.repository.AttachPoolRepository;
import com.ksptool.bio.biz.core.repository.AttachRepository;
import com.ksptool.bio.commons.config.AttachConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Slf4j
@Service
public class AttachPoolService {

    private static final int DEEP_SCAN_PAGE_SIZE = 500;

    //扫描锁
    private final ReentrantLock scanLock = new ReentrantLock();

    private volatile boolean rebuildRunning = false;

    private volatile String rebuildMessage = "空闲";

    private volatile LocalDateTime rebuildStartTime;

    private volatile LocalDateTime rebuildEndTime;

    private final AtomicInteger rebuildTotal = new AtomicInteger(0);

    private final AtomicInteger rebuildProcessed = new AtomicInteger(0);

    private final AtomicInteger rebuildImported = new AtomicInteger(0);

    private final AtomicInteger rebuildRepaired = new AtomicInteger(0);

    private final AtomicInteger rebuildDeleted = new AtomicInteger(0);

    private final AtomicInteger rebuildFailed = new AtomicInteger(0);

    @Autowired
    private AttachPoolRepository attachPoolRepository;

    @Autowired
    private AttachConfig attachConfig;

    @Autowired
    private AttachRepository attachRepository;

    @Autowired
    private AttachService attachService;

    @Autowired
    @Lazy
    private AttachPoolService self;

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

            Path poolRoot = resolvePoolRoot();

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
            if (scanLock.isHeldByCurrentThread()) {
                scanLock.unlock();
            }
        }
    }

    /**
     * 深度扫描附件池。校验已索引附件是否仍存在于磁盘，完成后更新统计数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deepScanAttachPool() throws BizException {
        if (!scanLock.tryLock()) {
            throw new BizException("附件池正在扫描中，请稍后再试");
        }
        try {
            long checkedCount = 0;
            long existingCount = 0;
            long missingCount = 0;

            while (true) {
                Page<AttachPo> page = attachRepository.getValidAttachList(PageRequest.of(0, DEEP_SCAN_PAGE_SIZE));
                if (page.isEmpty()) {
                    break;
                }

                for (AttachPo attach : page.getContent()) {
                    checkedCount++;
                    Path absolutePath = attachService.getAttachLocalPath(Paths.get(attach.getPath()));
                    if (!Files.exists(absolutePath)) {
                        log.warn("索引完整性校验：物理文件不存在 ID:{} 路径:{}", attach.getId(), absolutePath);
                        attach.setStatus(0);
                        attachRepository.save(attach);
                        missingCount++;
                        continue;
                    }
                    existingCount++;
                }
            }

            log.info("附件池索引完整性校验完成。校验总数:{} 仍存磁盘:{} 已丢失:{}", checkedCount, existingCount, missingCount);

            quickScanAttachPool();

        } finally {
            if (scanLock.isHeldByCurrentThread()) {
                scanLock.unlock();
            }
        }
    }

    /**
     * 启动重建索引异步任务
     */
    public synchronized void startRebuildIndex() throws BizException {
        if (rebuildRunning) {
            throw new BizException("重建索引任务进行中");
        }
        rebuildRunning = true;
        rebuildMessage = "任务启动中";
        rebuildStartTime = LocalDateTime.now();
        rebuildEndTime = null;
        rebuildTotal.set(0);
        rebuildProcessed.set(0);
        rebuildImported.set(0);
        rebuildRepaired.set(0);
        rebuildDeleted.set(0);
        rebuildFailed.set(0);
        SecurityContext asyncSecurityContext = SecurityContextHolder.createEmptyContext();
        asyncSecurityContext.setAuthentication(SecurityContextHolder.getContext().getAuthentication());
        CompletableFuture.runAsync(() -> {
            SecurityContextHolder.setContext(asyncSecurityContext);
            try {
                scanLock.lock();
                try {
                rebuildMessage = "重建索引进行中";
                Path poolRoot = resolvePoolRoot();

                Set<String> indexedPaths = new HashSet<>();
                for (String path : attachRepository.getAllPaths()) {
                    if (path == null) {
                        continue;
                    }
                    indexedPaths.add(path.replace('\\', '/'));
                }

                List<Path> driftFiles = new ArrayList<>();
                if (Files.exists(poolRoot)) {
                    Files.walkFileTree(poolRoot, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (!attrs.isRegularFile()) {
                                return FileVisitResult.CONTINUE;
                            }
                            String relative = poolRoot.relativize(file).toString().replace('\\', '/');
                            if (!indexedPaths.contains(relative)) {
                                driftFiles.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            log.warn("无法访问文件: {} - {}", file, exc.getMessage());
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }

                rebuildTotal.set(driftFiles.size());

                for (Path drift : driftFiles) {
                    try {
                        String sha256 = attachService.computeSha256(drift);
                        List<AttachPo> matches = attachRepository.getBySha256(sha256);
                        String displayName = drift.getFileName().toString();

                        if (matches.isEmpty()) {
                            attachService.ingestLocalFile(drift, displayName);
                            rebuildImported.incrementAndGet();
                            rebuildProcessed.incrementAndGet();
                            continue;
                        }

                        List<AttachPo> brokenList = new ArrayList<>();
                        for (AttachPo match : matches) {
                            if (match.getStatus() != null && match.getStatus() == 3) {
                                Path absolutePath = attachService.getAttachLocalPath(Paths.get(match.getPath()));
                                if (Files.exists(absolutePath)) {
                                    continue;
                                }
                            }
                            brokenList.add(match);
                        }

                        if (brokenList.isEmpty()) {
                            Files.deleteIfExists(drift);
                            rebuildDeleted.incrementAndGet();
                            rebuildProcessed.incrementAndGet();
                            continue;
                        }

                        boolean useCopy = brokenList.size() > 1;
                        int repairSuccess = 0;
                        for (AttachPo broken : brokenList) {
                            if (attachService.repairAttachFromDrift(drift, broken, useCopy)) {
                                repairSuccess++;
                            }
                        }
                        if (repairSuccess == 0) {
                            rebuildFailed.incrementAndGet();
                            rebuildProcessed.incrementAndGet();
                            continue;
                        }
                        rebuildRepaired.addAndGet(repairSuccess);
                        if (useCopy) {
                            Files.deleteIfExists(drift);
                        }
                        if (repairSuccess < brokenList.size()) {
                            rebuildFailed.addAndGet(brokenList.size() - repairSuccess);
                        }
                        rebuildProcessed.incrementAndGet();
                    } catch (Exception e) {
                        log.warn("重建索引处理游离文件失败: {} - {}", drift, e.getMessage());
                        rebuildFailed.incrementAndGet();
                        rebuildProcessed.incrementAndGet();
                    }
                }

                self.quickScanAttachPool();

                rebuildMessage = String.format("重建索引完成。新建:%d 修复:%d 删除:%d 失败:%d",
                        rebuildImported.get(), rebuildRepaired.get(), rebuildDeleted.get(), rebuildFailed.get());

            } catch (BizException e) {
                log.error("重建索引异常", e);
                rebuildMessage = "重建索引失败: " + e.getMessage();
            } finally {
                if (scanLock.isHeldByCurrentThread()) {
                    scanLock.unlock();
                }
                rebuildRunning = false;
                rebuildEndTime = LocalDateTime.now();
            }
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
    }

    /**
     * 查询重建索引任务进度
     *
     * @return 任务进度
     */
    public GetRebuildIndexStatusVo getRebuildIndexStatus() {
        var vo = new GetRebuildIndexStatusVo();
        vo.setRunning(rebuildRunning);
        vo.setTotal(rebuildTotal.get());
        vo.setProcessed(rebuildProcessed.get());
        vo.setImported(rebuildImported.get());
        vo.setRepaired(rebuildRepaired.get());
        vo.setDeleted(rebuildDeleted.get());
        vo.setFailed(rebuildFailed.get());
        vo.setMessage(rebuildMessage);
        vo.setStartTime(rebuildStartTime);
        vo.setEndTime(rebuildEndTime);
        return vo;
    }

    /**
     * 清除无效索引，仅删除 status 非 3 的数据库记录，不删除磁盘文件
     *
     * @return 操作摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public String clearInvalidIndexes() throws BizException {
        if (rebuildRunning) {
            throw new BizException("重建索引任务进行中");
        }
        if (!scanLock.tryLock()) {
            throw new BizException("附件池正在扫描中，请稍后再试");
        }
        try {
            long deletedCount = 0;
            while (true) {
                Page<AttachPo> page = attachRepository.getInvalidAttachList(PageRequest.of(0, DEEP_SCAN_PAGE_SIZE));
                if (page.isEmpty()) {
                    break;
                }
                attachRepository.deleteAll(page.getContent());
                deletedCount += page.getNumberOfElements();
            }

            log.info("清除无效索引完成，删除 {} 条", deletedCount);

            quickScanAttachPool();

            return String.format("已清除 %d 条无效索引", deletedCount);

        } finally {
            if (scanLock.isHeldByCurrentThread()) {
                scanLock.unlock();
            }
        }
    }

    /**
     * 解析当前操作系统的附件池根目录
     */
    private Path resolvePoolRoot() throws BizException {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return Paths.get(attachConfig.getLocalWindowsPath());
        }
        if (osName.contains("linux")) {
            return Paths.get(attachConfig.getLocalLinuxPath());
        }
        throw new BizException("不支持的操作系统: " + osName);
    }
}
