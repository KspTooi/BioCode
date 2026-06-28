package com.ksptool.bio.biz.core.service;


import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.core.model.attach.AttachPo;
import com.ksptool.bio.biz.core.model.attach.dto.PreCheckAttachDto;
import com.ksptool.bio.biz.core.model.attach.vo.ApplyChunkVo;
import com.ksptool.bio.biz.core.model.attach.vo.DirectUploadVo;
import com.ksptool.bio.biz.core.model.attach.vo.PreCheckAttachVo;
import com.ksptool.bio.biz.core.repository.AttachChunkRepository;
import com.ksptool.bio.biz.core.repository.AttachRepository;
import com.ksptool.bio.commons.config.AttachConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件附件服务(通用)
 *
 * @author KspTooi
 * @since 1.0.1(A).1
 */
@Service
@Slf4j
public class AttachService {

    public static final String REBUILD_INDEX_KIND = "rebuild_index";

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd");
    private final Tika tika = new Tika();

    @Autowired
    private AttachRepository repository;

    @Autowired
    private AttachChunkRepository chunkRepository;

    @Autowired
    private AttachConfig attachConfig;

    @Autowired
    @Lazy
    private AttachService self;

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 根据文件大小和分块大小计算分块数量
     *
     * @param bytes     文件大小
     * @param chunkSize 分块大小
     * @return 分块数量
     */
    public static Long getChunkCount(Long bytes, Long chunkSize) {
        return (bytes + chunkSize - 1) / chunkSize;
    }

    /**
     * 获取分块大小
     *
     * @return 分块大小 5MB
     */
    public static long getChunkSize() {
        return 5 * 1024 * 1024;
    }

    /**
     * 获取文件后缀
     *
     * @param name 文件名
     * @return 文件后缀
     */
    public static String getSuffix(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }

        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == name.length() - 1) {
            return "";
        }

        return name.substring(lastDotIndex + 1);
    }

    /**
     * 零散文件上传
     *
     * @param file 文件
     * @return 上传结果
     * @throws BizException 上传失败
     */
    @Transactional(rollbackFor = Exception.class)
    public DirectUploadVo directUpload(MultipartFile file, String kind) throws BizException {

        if (file == null || file.isEmpty()) {
            throw new BizException("文件不能为空");
        }

        if (StringUtils.isBlank(kind)) {
            throw new BizException("业务代码不能为空");
        }

        var aId = uploadAttach(file, kind);
        var attach = repository.findById(aId).orElseThrow(() -> new BizException("附件不存在"));
        var vo = new DirectUploadVo();
        vo.setId(attach.getId());
        vo.setName(attach.getName());
        vo.setTotalSize(attach.getTotalSize());
        vo.setKind(attach.getKind());
        vo.setPath(attach.getPath());
        vo.setStatus(attach.getStatus());
        vo.setCreateTime(attach.getCreateTime());
        return vo;
    }

    /**
     * 预览附件
     *
     * @param path 文件路径
     * @return 附件预览
     * @throws BizException 预览附件失败
     */
    public ResponseEntity<Resource> directPreview(String path) throws Exception {

        if (StringUtils.isBlank(path)) {
            throw new BizException("文件路径不能为空");
        }

        var absolutePath = getAttachLocalPath(Paths.get(path));

        if (!Files.exists(absolutePath)) {
            throw new BizException("文件不存在");
        }

        var resource = new FileSystemResource(absolutePath);

        //探测Mine类型
        var mimeType = tika.detect(resource.getFile());
        var filename = URLEncoder.encode(path, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }


    /**
     * 上传附件
     *
     * @param file 附件文件
     * @return 附件ID
     * @throws BizException 上传附件失败
     */
    public Long uploadAttach(MultipartFile file) throws BizException {
        return uploadAttach(file, "internal");
    }

    /**
     * 上传附件
     *
     * @param file 附件文件
     * @param kind 附件类型
     * @return 附件ID
     * @throws BizException 上传附件失败
     */
    @Transactional(rollbackFor = Exception.class)
    public Long uploadAttach(MultipartFile file, String kind) throws BizException {

        if (file == null || file.isEmpty()) {
            throw new BizException("附件不能为空");
        }

        if (StringUtils.isBlank(kind)) {
            throw new BizException("附件类型不能为空");
        }

        var filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            filename = file.getName();
        }

        if (StringUtils.isBlank(filename)) {
            throw new BizException("附件文件名不能为空");
        }

        var suffix = getSuffix(filename);
        var totalSize = file.getSize();
        if (totalSize <= 0) {
            throw new BizException("附件大小无效");
        }

        Path tmpFile = null;
        try {
            tmpFile = Files.createTempFile("eas_attach_", ".tmp");

            var digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = file.getInputStream(); OutputStream os = Files.newOutputStream(tmpFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                    os.write(buffer, 0, read);
                }
            }

            var sha256 = bytesToHex(digest.digest());

            var exist = repository.getBySha256AndKind(sha256, kind);
            if (exist != null && exist.getStatus() != null && exist.getStatus() == 3) {
                Files.deleteIfExists(tmpFile);
                return exist.getId();
            }

            var relativePath = getAttachRelativePath(sha256, suffix);
            if (relativePath == null) {
                throw new BizException("获取附件本地路径失败");
            }

            var absolutePath = getAttachLocalPath(relativePath);
            var parentDir = absolutePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            if (Files.exists(absolutePath)) {
                Files.deleteIfExists(tmpFile);
            }

            if (!Files.exists(absolutePath)) {
                Files.move(tmpFile, absolutePath);
            }

            var po = new AttachPo();
            po.setName(filename);
            po.setKind(kind);
            po.setSuffix(suffix);
            po.setPath(relativePath.toString());
            po.setSha256(sha256);
            po.setTotalSize(totalSize);
            po.setReceiveSize(totalSize);
            po.setStatus(3);
            po.setVerifyTime(LocalDateTime.now());
            po.setChunks(new ArrayList<>());
            repository.save(po);
            return po.getId();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("上传附件失败: " + e.getMessage());
        } finally {
            if (tmpFile != null) {
                try {
                    Files.deleteIfExists(tmpFile);
                } catch (Exception ignore) {
                }
            }
        }
    }

    public AttachPo requireAttach(Long id) throws BizException {
        if (id == null) {
            throw new BizException("附件ID不能为空");
        }
        return repository.findById(id).orElseThrow(() -> new BizException("附件不存在"));
    }

    /**
     * 预检文件
     *
     * @param dto 预检文件参数
     * @return 预检文件结果
     * @throws BizException 预检文件失败
     */
    @Transactional(rollbackFor = Exception.class)
    public PreCheckAttachVo preCheckAttach(PreCheckAttachDto dto) throws BizException {

        var ret = new PreCheckAttachVo();
        //ret.setPreCheckId(IdWorker.nextId());

        //校验文件是否存在
        var attach = repository.getBySha256AndKind(dto.getSha256(), dto.getKind());

        if (attach == null) {
            var relativePath = getAttachRelativePath(dto.getSha256(), getSuffix(dto.getName()));

            if (relativePath == null) {
                throw new BizException("获取附件本地路径失败");
            }

            var absolutePath = getAttachLocalPath(relativePath);

            //创建预检记录
            AttachPo insertPo = new AttachPo();
            insertPo.setName(dto.getName());
            insertPo.setKind(dto.getKind());
            insertPo.setSuffix(getSuffix(dto.getName()));
            insertPo.setPath(relativePath.toString());
            insertPo.setSha256(dto.getSha256());
            insertPo.setTotalSize(dto.getTotalSize());
            insertPo.setReceiveSize(0L);
            insertPo.setStatus(0);
            insertPo.setChunks(new ArrayList<>());
            insertPo = repository.save(insertPo);

            ret.setPreCheckId(insertPo.getId());
            ret.setName(insertPo.getName());
            ret.setKind(insertPo.getKind());
            ret.setPath(insertPo.getPath());
            ret.setStatus(0);
            long chunkCount = getChunkCount(dto.getTotalSize(), getChunkSize());
            List<Long> missingChunkIds = new ArrayList<>();
            for (long i = 0; i < chunkCount; i++) {
                missingChunkIds.add(i);
            }
            ret.setMissingChunkIds(missingChunkIds);

            //预分配文件
            preAllocateAttach(absolutePath, dto.getTotalSize());
            return ret;
        }

        ret.setPreCheckId(attach.getId());

        //0:预检文件 1:区块不完整 2:有效
        if (attach.getStatus() == 0 || attach.getStatus() == 1) {
            ret.setPreCheckId(attach.getId());
            ret.setName(attach.getName());
            ret.setKind(attach.getKind());
            ret.setPath(Paths.get(attach.getPath()).toString());
            ret.setStatus(1);

            //计算缺失分块
            long chunkCount = getChunkCount(attach.getTotalSize(), getChunkSize());
            Set<Long> existChunkIds = new HashSet<>();
            for (var chunk : attach.getChunks()) {
                existChunkIds.add(chunk.getChunkId());
            }
            List<Long> missingChunkIds = new ArrayList<>();

            for (long i = 0; i < chunkCount; i++) {
                if (!existChunkIds.contains(i)) {
                    missingChunkIds.add(i);
                }
            }
            ret.setMissingChunkIds(missingChunkIds);
            return ret;
        }

        //2:有效
        if (attach.getStatus() == 2) {
            ret.setName(attach.getName());
            ret.setKind(attach.getKind());
            ret.setPath(Paths.get(attach.getPath()).toString());
            ret.setStatus(2);
            ret.setMissingChunkIds(Collections.emptyList());
            return ret;
        }

        //3:校验中
        if (attach.getStatus() == 3) {
            ret.setName(attach.getName());
            ret.setKind(attach.getKind());
            ret.setPath(Paths.get(attach.getPath()).toString());
            ret.setStatus(3);
            ret.setMissingChunkIds(Collections.emptyList());
            return ret;
        }

        return ret;
    }

    @Transactional(rollbackFor = Exception.class)
    public ApplyChunkVo applyChunk(Long preCheckId, Long chunkId, MultipartFile file) throws BizException {

        var attach = repository.findById(preCheckId).orElseThrow(() -> new BizException("预检文件不存在"));
        var chunkSize = file.getSize();
        var chunkCount = getChunkCount(attach.getTotalSize(), getChunkSize());

        if (attach.getStatus() == 2 || attach.getStatus() == 3) {
            throw new BizException("附件已完整或正在进行校验,无法应用区块!");
        }

        if (chunkSize > attach.getTotalSize()) {
            throw new BizException("区块大小超过文件总大小!");
        }

        if (chunkId >= chunkCount || chunkId < 0) {
            throw new BizException("区块ID范围错误! 最大为:" + chunkCount);
        }

        if ((attach.getReceiveSize() + chunkSize) > attach.getTotalSize()) {
            throw new BizException("应用该区块后,文件大小将超过总大小!");
        }

        //查询是否已重复应用
        var count = chunkRepository.countByAttachIdAndChunkId(attach.getId(), chunkId);

        if (count > 0) {
            throw new BizException("该区块已被应用.");
        }

        var absolutePath = getAttachLocalPath(Paths.get(attach.getPath()));

        if (!Files.exists(absolutePath)) {
            throw new BizException("文件系统错误,文件不存在,请确认该附件已预检过: " + absolutePath);
        }

        //计算写入位置
        long offset = chunkId * getChunkSize();

        //写入区块到文件中
        try (RandomAccessFile raf = new RandomAccessFile(absolutePath.toString(), "rw");
             InputStream inputStream = file.getInputStream()) {

            raf.seek(offset);
            byte[] buffer = new byte[8192];
            long remaining = chunkSize;

            while (remaining > 0) {
                int bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (bytesRead == -1) {
                    break;
                }
                raf.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        } catch (IOException e) {
            log.error("写入区块失败 附件ID:{} 区块ID:{} 错误:{}", attach.getId(), chunkId, e.getMessage(), e);
            throw new BizException("写入区块失败: " + e.getMessage());
        }


        var ret = new ApplyChunkVo();
        ret.setAttachId(attach.getId());
        ret.setChunkTotal(chunkCount.intValue());

        //保存分块记录
        attach.addChunk(chunkId);

        //更新已接收大小
        attach.setReceiveSize(attach.getReceiveSize() + chunkSize);

        //检查是否所有分块都已上传完成
        var chunkPos = attach.getChunks();

        ret.setChunkApplied(chunkPos.size());

        if (chunkPos.size() != chunkCount) {
            return ret;
        }

        //所有分块都已上传完成 更新为校验中状态
        if (chunkPos.size() == chunkCount) {
            attach.setStatus(2);
        }

        repository.save(attach);
        return ret;
    }


    /**
     * 获取文件本地路径
     *
     * @return 文件本地路径 如果获取失败返回null
     * 按日期创建文件夹 /root/2025_12_18/SHA256
     */
    public Path getAttachLocalPath(Path relativePath) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return Paths.get(attachConfig.getLocalWindowsPath()).resolve(relativePath);
        }
        if (osName.contains("linux")) {
            return Paths.get(attachConfig.getLocalLinuxPath()).resolve(relativePath);
        }
        throw new RuntimeException("不支持的操作系统: " + osName);
    }

    /**
     * 获取文件相对路径
     *
     * @return 文件相对路径 如果获取失败返回null
     * 按日期创建文件夹 /2025_12_18/SHA256
     */
    public Path getAttachRelativePath(String sha256, String suffix) {

        String osName = System.getProperty("os.name").toLowerCase();

        String storePath = null;

        if (osName.contains("win")) {
            storePath = attachConfig.getLocalWindowsPath();
        }

        if (osName.contains("linux")) {
            storePath = attachConfig.getLocalLinuxPath();
        }

        if (!osName.contains("win") && !osName.contains("linux")) {
            return null;
        }

        if (storePath == null) {
            return null;
        }

        var _suffix = StringUtils.isBlank(suffix) ? "" : "." + suffix;
        var _date = dateFormat.format(LocalDateTime.now());

        var rootPath = Paths.get(storePath);
        var absolutePathDir = rootPath.resolve(_date);
        var relativePathFile = Paths.get(_date).resolve(sha256 + _suffix);

        if (!Files.exists(absolutePathDir)) {
            try {
                Files.createDirectories(absolutePathDir);
            } catch (IOException e) {
                log.error("自动创建资源文件夹失败 路径:{} 错误:{}", storePath, e.getMessage(), e);
                return null;
            }
        }

        return relativePathFile;
    }

    /**
     * 预分配文件 写入一个指定大小的占位文件
     *
     * @throws BizException 预分配文件失败
     */
    public void preAllocateAttach(Path path, Long totalSize) throws BizException {

        if (path == null || path.toString().isBlank()) {
            throw new BizException("附件路径不能为空");
        }

        if (totalSize == null || totalSize <= 0) {
            throw new BizException("附件大小无效");
        }

        if (Files.exists(path)) {

            boolean moveSuccess = false;
            int maxRetries = 3; // 最大重试3次

            // 尝试重命名循环
            for (int i = 0; i < maxRetries; i++) {
                try {
                    // 当预分配文件存在但数据库无数据时 将旧的预分配文件重命名
                    var uuid = UUID.randomUUID().toString().replace("-", "");
                    var newPath = path.getParent().resolve("conflicted_" + uuid + "." + path.getFileName().toString());
                    Files.move(path, newPath);
                    log.info("已将旧的预分配文件 {} 重命名为: {}", path.getFileName(), newPath.getFileName().toString());
                    moveSuccess = true;
                    break; // 成功则跳出循环
                } catch (IOException e) {
                    log.warn("重命名文件失败，可能是文件被锁定，正在重试 ({}/{}): {}", i + 1, maxRetries, e.getMessage());
                    try {
                        // 休眠 100ms 等待锁释放（如杀毒软件扫描结束）
                        Thread.sleep(100);
                        // 在极端的 Windows 情况下，有时建议调用 System.gc() 强制释放未关闭的 MappedByteBuffer，但不建议作为首选
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            if (!moveSuccess) {
                // 如果重试多次依然失败，抛出异常
                throw new BizException("无法预分配文件，文件被系统锁定且重命名失败: " + path);
            }

        }

        try {
            var parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (RandomAccessFile raf = new RandomAccessFile(path.toString(), "rw")) {
                raf.setLength(totalSize);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BizException("预分配文件失败: " + e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 5000) //5秒执行一次
    public void verifyAttach() {

        //查询全部需要校验的文件附件
        var attachList = repository.getNeedVerifyAttachList(500);

        if (attachList.isEmpty()) {
            return;
        }

        var success = 0;
        var error = 0;

        log.info("开始校验附件 数量:{}", attachList.size());

        for (var attach : attachList) {
            if (self.verifyAttachInternal(attach)) {
                success++;
                continue;
            }
            error++;
        }

        log.info("已完成:{} 个附件的校验. 成功:{} 个 失败:{} 个", attachList.size(), success, error);
    }

    /**
     * 校验文件
     *
     * @param attach 文件附件
     * @return 是否校验成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean verifyAttachInternal(AttachPo attach) {

        var absolutePath = getAttachLocalPath(Paths.get(attach.getPath()));

        if (!Files.exists(absolutePath)) {
            log.error("校验文件不存在 ID:{} 路径:{}", attach.getId(), absolutePath);
            attach.setStatus(0);
            repository.save(attach);
            return false;
        }

        try (InputStream is = Files.newInputStream(absolutePath)) {

            var digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            String actualSha256 = sb.toString();

            // 校验失败
            if (!actualSha256.equals(attach.getSha256())) {
                log.error("文件校验失败 ID:{} 预期:{} 实际:{}", attach.getId(), attach.getSha256(), actualSha256);
                attach.setStatus(1); // 回退到 1:区块不完整
                return false;
            }

            // 校验成功
            attach.setStatus(3); // 3:有效
            attach.setVerifyTime(LocalDateTime.now());
            //log.info("文件校验成功 ID:{} SHA256:{}", attach.getId(), actualSha256);
            return true;
        } catch (Exception e) {
            log.error("文件校验异常 ID:{} 错误:{}", attach.getId(), e.getMessage(), e);
            attach.setStatus(1); // 发生异常也回退状态
            return false;
        } finally {
            repository.save(attach);
        }
    }

    /**
     * 流式计算文件SHA256摘要
     *
     * @param file 文件路径
     * @return 摘要十六进制字符串
     */
    public String computeSha256(Path file) throws BizException {
        try (InputStream is = Files.newInputStream(file)) {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            throw new BizException("计算文件摘要失败: " + e.getMessage());
        }
    }

    /**
     * 从本地游离文件导入索引，kind固定为rebuild_index
     *
     * @param source      游离文件绝对路径
     * @param displayName 展示文件名
     * @return 是否成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean ingestLocalFile(Path source, String displayName) throws BizException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new BizException("游离文件不存在");
        }

        var filename = displayName;
        if (StringUtils.isBlank(filename)) {
            filename = source.getFileName().toString();
        }
        if (StringUtils.isBlank(filename)) {
            throw new BizException("文件名不能为空");
        }

        var suffix = getSuffix(filename);
        long totalSize;
        try {
            totalSize = Files.size(source);
        } catch (IOException e) {
            throw new BizException("读取文件大小失败: " + e.getMessage());
        }
        if (totalSize <= 0) {
            throw new BizException("文件大小无效");
        }

        var sha256 = computeSha256(source);

        var exist = repository.getBySha256AndKind(sha256, REBUILD_INDEX_KIND);
        if (exist != null && exist.getStatus() != null && exist.getStatus() == 3) {
            try {
                Files.deleteIfExists(source);
            } catch (IOException e) {
                throw new BizException("删除重复游离文件失败: " + e.getMessage());
            }
            return true;
        }

        var relativePath = getAttachRelativePath(sha256, suffix);
        if (relativePath == null) {
            throw new BizException("获取附件本地路径失败");
        }

        var absolutePath = getAttachLocalPath(relativePath);
        var parentDir = absolutePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw new BizException("创建附件目录失败: " + e.getMessage());
            }
        }

        var sourceAbs = source.toAbsolutePath().normalize();
        var targetAbs = absolutePath.toAbsolutePath().normalize();
        var alreadyAtTarget = sourceAbs.equals(targetAbs);
        if (!alreadyAtTarget) {
            try {
                alreadyAtTarget = Files.isSameFile(source, absolutePath);
            } catch (IOException ignore) {
            }
        }

        try {
            if (alreadyAtTarget) {
                //文件已在标准路径，仅补建索引
            }
            if (!alreadyAtTarget && Files.exists(absolutePath)) {
                Files.deleteIfExists(source);
            }
            if (!alreadyAtTarget && !Files.exists(absolutePath)) {
                Files.move(source, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BizException("移动游离文件失败: " + e.getMessage());
        }

        var indexPath = relativePath.toString().replace('\\', '/');
        if (alreadyAtTarget) {
            String osName = System.getProperty("os.name").toLowerCase();
            Path poolRoot = null;
            if (osName.contains("win")) {
                poolRoot = Paths.get(attachConfig.getLocalWindowsPath()).toAbsolutePath().normalize();
            }
            if (osName.contains("linux")) {
                poolRoot = Paths.get(attachConfig.getLocalLinuxPath()).toAbsolutePath().normalize();
            }
            if (poolRoot != null && sourceAbs.startsWith(poolRoot)) {
                indexPath = poolRoot.relativize(sourceAbs).normalize().toString().replace('\\', '/');
            }
        }

        var po = new AttachPo();
        po.setName(filename);
        po.setKind(REBUILD_INDEX_KIND);
        po.setSuffix(suffix);
        po.setPath(indexPath);
        po.setSha256(sha256);
        po.setTotalSize(totalSize);
        po.setReceiveSize(totalSize);
        po.setStatus(3);
        po.setVerifyTime(LocalDateTime.now());
        po.setChunks(new ArrayList<>());
        repository.save(po);
        return true;
    }

    /**
     * 用游离文件修复索引记录
     *
     * @param drift  游离文件绝对路径
     * @param target 目标索引记录
     * @param copy   是否复制，false则移动
     * @return 是否修复成功
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean repairAttachFromDrift(Path drift, AttachPo target, boolean copy) throws BizException {
        if (drift == null || !Files.isRegularFile(drift)) {
            throw new BizException("游离文件不存在");
        }
        if (target == null || target.getId() == null) {
            throw new BizException("索引记录无效");
        }

        var attach = repository.findById(target.getId()).orElseThrow(() -> new BizException("索引记录不存在"));
        var targetAbs = getAttachLocalPath(Paths.get(attach.getPath()));
        var parentDir = targetAbs.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw new BizException("创建附件目录失败: " + e.getMessage());
            }
        }

        try {
            if (copy) {
                Files.copy(drift, targetAbs, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!copy) {
                Files.move(drift, targetAbs, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BizException("写入索引文件失败: " + e.getMessage());
        }

        var actualSha256 = computeSha256(targetAbs);
        if (!actualSha256.equals(attach.getSha256())) {
            log.warn("重建索引校验失败 ID:{} 预期:{} 实际:{}", attach.getId(), attach.getSha256(), actualSha256);
            return false;
        }

        long fileSize;
        try {
            fileSize = Files.size(targetAbs);
        } catch (IOException e) {
            throw new BizException("读取文件大小失败: " + e.getMessage());
        }

        attach.setTotalSize(fileSize);
        attach.setReceiveSize(fileSize);
        attach.setStatus(3);
        attach.setVerifyTime(LocalDateTime.now());
        repository.save(attach);
        return true;
    }


}