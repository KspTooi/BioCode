package com.ksptool.bio.biz.drive.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.core.model.attach.AttachPo;
import com.ksptool.bio.biz.core.repository.AttachRepository;
import com.ksptool.bio.biz.core.service.AttachService;
import com.ksptool.bio.biz.drive.model.driveentry.EntryPo;
import com.ksptool.bio.biz.drive.model.driveentry.dto.*;
import com.ksptool.bio.biz.drive.model.driveentry.vo.*;
import com.ksptool.bio.biz.drive.repository.EntryRepository;
import com.ksptool.bio.commons.utils.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.ksptool.bio.biz.auth.service.SessionService.session;
import static com.ksptool.entities.Entities.as;

@Slf4j
@Service
public class EntryService {

    @Autowired
    private EntryRepository repository;

    @Autowired
    private AttachRepository attachRepository;

    @Autowired
    private AttachService attachService;

    /**
     * 获取云盘信息
     *
     * @return 云盘信息
     */
    public GetDriveInfo getDriveInfo() throws Exception {

        var companyId = session().getRootId();
        var ret = repository.getDriveInfo();

        //总容量为2TB
        ret.setTotalCapacity(2L * 1024 * 1024 * 1024 * 1024);
        return ret;
    }

    /**
     * 查询条目列表
     *
     * @param dto 查询条件
     * @return 条目列表
     */
    public GetEntryListVo getEntryList(GetEntryListDto dto) throws Exception, BizException {

        Long dirId = null;
        String dirName = null;
        Long dirParentId = null;
        List<GetEntryListPathVo> pathVos = new ArrayList<>();

        //查询目录详情
        if (dto.getDirectoryId() != null) {
            var dirEntryPo = repository.getEntryById(dto.getDirectoryId());

            if (dirEntryPo == null || dirEntryPo.getKind() != 1) {
                throw new BizException("指定的目录不存在或无权限访问");
            }
            dirId = dirEntryPo.getId();
            dirName = dirEntryPo.getName();
            dirParentId = dirEntryPo.getParent() != null ? dirEntryPo.getParent().getId() : null;

            //查询当前的目录路径(至多10层)
            var reamingLevel = 10;
            var _parent = dirEntryPo;

            //添加当前文件夹作为最后一级路径
            pathVos.add(GetEntryListPathVo.of(dirEntryPo));

            while (reamingLevel > 0) {

                var parent = _parent.getParent();

                if (parent == null) {
                    pathVos.add(GetEntryListPathVo.ofRoot());
                    break;
                }

                pathVos.add(GetEntryListPathVo.of(parent));
                _parent = parent;
                reamingLevel--;
            }


        }

        var ret = new GetEntryListVo();
        ret.setDirId(dirId);
        ret.setDirName(dirName);
        ret.setDirParentId(dirParentId);
        ret.setPaths(pathVos);

        Page<EntryPo> page = repository.getEntryList(dto.getDirectoryId(), dto.getKeyword(), dto.pageRequest());
        ret.setTotal(page.getTotalElements());
        ret.setItems(as(page.getContent(), GetEntryListItemVo.class));
        return ret;
    }

    /**
     * 新增条目
     *
     * @param dto 新增条目
     */
    @Transactional(rollbackFor = Exception.class)
    public void addEntry(AddEntryDto dto) throws BizException, Exception {

        var userId = session().getUserId();

        if (repository.countByName(dto.getParentId(), dto.getName()) > 0) {
            throw new BizException("指定的条目与已存在的条目名称重复");
        }

        //组装新增数据
        EntryPo insertPo = as(dto, EntryPo.class);

        //文件夹
        if (dto.getKind() == 1) {
            insertPo.setKind(1);
            insertPo.setAttach(null);
            insertPo.setAttachSize(0L);
            insertPo.setAttachSuffix(null);
        }

        //文件
        if (dto.getKind() == 0) {
            var attachPo = attachRepository.findById(dto.getAttachId()).orElseThrow(() -> new BizException("指定的文件附件不存在."));

            //if (!Objects.equals(attachPo.getCreatorId(), userId)) {
            //    throw new BizException("指定的文件附件不属于当前用户,无法新增驱动器条目.");
            //}

            insertPo.setKind(0);
            insertPo.setAttach(attachPo);
            insertPo.setAttachSize(attachPo.getTotalSize());
            insertPo.setAttachSuffix(attachPo.getSuffix());
            insertPo.setAttachStatus(attachPo.getStatus());
        }

        //挂载到父级条目
        if (dto.getParentId() != null) {
            var parentPo = repository.findById(dto.getParentId()).orElseThrow(() -> new BizException("指定的父级条目不存在."));

            if (parentPo.getKind() != 1) {
                throw new BizException("指定的挂载父级目录必须是文件夹。");
            }

            insertPo.setParent(parentPo);
            parentPo.getChildren().add(insertPo);
        }

        //保存条目
        repository.save(insertPo);
    }

    /**
     * 复制条目
     *
     * @param dto 复制条目
     */
    @Transactional(rollbackFor = Exception.class)
    public void copyEntry(CopyEntryDto dto) throws BizException, Exception {

        //查询要复制的条目
        var entryPos = repository.getEntryByIds(dto.getEntryIds());

        if (entryPos.isEmpty()) {
            throw new BizException("要复制的条目不存在或无权限访问");
        }

        //查找父级条目
        EntryPo parentPo = null;

        if (dto.getParentId() != null) {
            parentPo = repository.getEntryById(dto.getParentId());

            if (parentPo == null) {
                throw new BizException("指定的父级条目不存在或无权限访问");
            }

            if (parentPo.getKind() != 1) {
                throw new BizException("指定的挂载父级目录必须是文件夹。");
            }
        }

        // 1. 获取目标目录下所有的已存在文件名 (避免在循环中反复查库)
        // 注意：Repository 需要支持 getNamesByParentId，如果不支持，可以使用原有的 count 逻辑，但在循环里效率较低
        // 这里为了兼容现有代码，我们先初始化一个空的 Set，如果 repository 没有直接获取所有名字的方法，
        // 我们可以依然用 count 判断，但为了处理批量复制时的内部冲突，我们需要维护一个本次操作的 usedNames
        Set<String> usedNames = new HashSet<>();
        // 如果 repository 有 getNamesByParentId 方法最好，如果没有，我们依靠下方的 while 循环 + 实时查库 + 本地 Set 缓存
        // 假设没有 getNamesByParentId，我们采用 "查库 + 本地缓存" 双重校验

        // 组装复制数据
        var copyPos = copyEntry(entryPos);

        for (var entryPo : copyPos) {

            entryPo.setParent(parentPo);
            String originalName = entryPo.getName();
            String finalName = originalName;

            // 生成规则：原名 -> 原名-副本 -> 原名-副本(1) -> 原名-副本(2)...

            // 第一次检查：是否直接冲突
            if (isNameConflict(dto.getParentId(), finalName, usedNames)) {

                // 尝试 "名称-副本"
                finalName = originalName + "-副本";

                // 如果 "名称-副本" 还冲突，开始循环递增 (1), (2)...
                int index = 1;
                while (isNameConflict(dto.getParentId(), finalName, usedNames)) {
                    finalName = originalName + "-副本(" + index + ")";
                    index++;
                }
            }

            // 确定了唯一名称
            entryPo.setName(finalName);
            // 将新名称加入本地占用表，防止本次批量操作中的后续文件与当前文件重名
            usedNames.add(finalName);
        }

        //保存复制条目
        repository.saveAll(copyPos);
    }

    /**
     * 辅助方法：检测名称是否冲突
     * 冲突条件：数据库里存在 OR 本次批量操作已占用了该名字
     */
    private boolean isNameConflict(Long parentId, String name, Set<String> currentBatchUsedNames) {
        // 1. 先看本次批量操作是否已经用过这个名字
        if (currentBatchUsedNames.contains(name)) {
            return true;
        }
        // 2. 再看数据库里是否已存在
        return repository.countByNameParentId(parentId, name) > 0;
    }


    /**
     * 重命名条目
     *
     * @param dto 重命名条目
     */
    @Transactional(rollbackFor = Exception.class)
    public void renameEntry(RenameEntry dto) throws BizException, AuthException {

        EntryPo updatePo = repository.findById(dto.getEntryId())
                .orElseThrow(() -> new BizException("重命名失败,数据不存在."));

        Long parentId = null;

        if (updatePo.getParent() != null) {
            parentId = updatePo.getParent().getId();
        }

        if (repository.countByNameParentId(parentId, dto.getName()) > 0) {
            throw new BizException("此位置已存在同名文件,无法重命名.");
        }
        updatePo.setName(dto.getName());
        updatePo.setUpdaterId(session().getUserId());
        updatePo.setUpdateTime(LocalDateTime.now());
        repository.save(updatePo);
    }

    /**
     * 条目移动检测
     *
     * @param dto 条目移动检测
     */
    public CheckEntryMoveVo checkEntryMove(MoveEntryDto dto) throws BizException, AuthException {

        var ret = new CheckEntryMoveVo();
        ret.setCanMove(2); //0:可以移动 1:名称冲突 2:不可移动

        Long targetId = null;

        //移动到非根目录 
        if (dto.getTargetId() != null) {

            var targetEntryPo = repository.getEntryById(dto.getTargetId());

            if (targetEntryPo == null) {
                ret.setCanMove(2);
                ret.setMessage("目标目录不存在或无权限访问");
                return ret;
            }

            if (targetEntryPo.getKind() != 1) {
                ret.setCanMove(2);
                ret.setMessage("目标必须是文件夹");
                return ret;
            }

            //检测是否有自身移动到自身内部
            if (dto.getEntryIds().contains(dto.getTargetId())) {
                ret.setCanMove(2);
                ret.setMessage("不能将文件或目录移动到自身目录下");
                return ret;
            }

            targetId = dto.getTargetId();
        }

        Set<String> names = repository.getNamesByIds(dto.getEntryIds());

        if (names.isEmpty()) {
            throw new BizException("要移动的文件/目录不存在或无权限访问");
        }

        //查找出目标条目下的重复项
        var matchNames = repository.matchNamesByParentId(names, targetId);

        if (!matchNames.isEmpty()) {
            ret.setCanMove(1);
            ret.setMessage("目标目录下存在同名文件.");
            ret.setConflictNames(matchNames);
            return ret;
        }

        ret.setCanMove(0);
        ret.setMessage("可以移动");
        return ret;
    }

    /**
     * 移动条目
     *
     * @param dto 移动条目
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveEntry(MoveEntryDto dto) throws BizException, AuthException {

        EntryPo targetDir = null;

        //移动到非根目录
        if (dto.getTargetId() != null) {

            targetDir = repository.getEntryById(dto.getTargetId());

            if (targetDir == null) {
                throw new BizException("目标条目不存在或无权限访问.");
            }

            if (targetDir.getKind() != 1) {
                throw new BizException("目标条目必须是文件夹.");
            }

            if (dto.getEntryIds().contains(dto.getTargetId())) {
                throw new BizException("不能将条目移动到自身内部.");
            }

        }


        //查找被移动条目
        var moveEntryPos = repository.getEntryByIds(dto.getEntryIds());

        if (moveEntryPos.isEmpty()) {
            throw new BizException("要移动的条目不存在或无权限访问.");
        }

        var moveEntryNames = new HashSet<String>();
        for (var entryPo : moveEntryPos) {
            moveEntryNames.add(entryPo.getName());
        }


        //覆盖模式移动
        if (dto.getMode() == 0) {

            //删除目标下全部重名条目
            repository.removeByNameAndParentId(moveEntryNames, dto.getTargetId());

            //将被移动条目挂载到目标条目
            for (var entryPo : moveEntryPos) {
                entryPo.setParent(targetDir);
            }

            //保存被移动条目
            repository.saveAll(moveEntryPos);
            return;
        }

        //跳过模式移动

        //查找目标条目下的重复项
        var matchNames = repository.matchNamesByParentId(moveEntryNames, dto.getTargetId());

        for (var entryPo : moveEntryPos) {

            //不移动重名条目
            if (matchNames.contains(entryPo.getName())) {
                continue;
            }

            entryPo.setParent(targetDir);
        }

        //保存被移动条目
        repository.saveAll(moveEntryPos);
    }

    /**
     * 查询条目详情
     *
     * @param dto 查询条件
     * @return 条目详情
     */
    public GetEntryDetailsVo getEntryDetails(DriveCommonIdDto dto) throws BizException {
        EntryPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));
        return as(po, GetEntryDetailsVo.class);
    }

    /**
     * 删除条目
     *
     * @param dto 删除条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeEntry(DriveCommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
        }
        if (!dto.isBatch()) {
            repository.deleteById(dto.getId());
        }
    }

    /**
     * 复制条目
     *
     * @param entryPos 条目列表
     * @return 新条目列表
     */
    public List<EntryPo> copyEntry(List<EntryPo> entryPos) throws BizException, AuthException {

        var result = new ArrayList<EntryPo>();

        for (var entryPo : entryPos) {
            var newEntry = copyEntryRecursive(entryPo, null);
            result.add(newEntry);
        }

        return result;
    }

    /**
     * 递归复制条目
     *
     * @param sourcePo 源条目
     * @param parentPo 父级条目
     * @return 新条目
     */
    private EntryPo copyEntryRecursive(EntryPo sourcePo, EntryPo parentPo) throws BizException {

        var newEntry = new EntryPo();
        newEntry.setId(IdWorker.nextId());
        newEntry.setParent(parentPo);
        newEntry.setName(sourcePo.getName());
        newEntry.setKind(sourcePo.getKind());
        newEntry.setAttach(sourcePo.getAttach());
        newEntry.setAttachSize(sourcePo.getAttachSize());
        newEntry.setAttachSuffix(sourcePo.getAttachSuffix());
        newEntry.setAttachStatus(sourcePo.getAttachStatus());

        if (sourcePo.getChildren() != null && !sourcePo.getChildren().isEmpty()) {
            var children = new ArrayList<EntryPo>();
            for (var child : sourcePo.getChildren()) {
                var newChild = copyEntryRecursive(child, newEntry);
                children.add(newChild);
            }
            newEntry.setChildren(new HashSet<>(children));
        }

        return newEntry;
    }


    /**
     * 定时任务 5秒执行一次,检查entrySyncList中的数据,如果存在数据,则执行同步操作
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional(rollbackFor = Exception.class)
    public void syncEntry() {

        //查询全部需要同步的云盘条目
        var entryPos = repository.getNeedSyncEntryList(500);

        if (entryPos.isEmpty()) {
            return;
        }

        log.info("正在检查:{} 个云盘条目", entryPos.size());

        var attachIds = new ArrayList<Long>();

        //获取所有需要同步的文件附件ID
        for (EntryPo entryPo : entryPos) {
            if (entryPo.getAttach() != null) {
                attachIds.add(entryPo.getAttach().getId());
            }
        }

        //获取所有需要同步的文件附件
        var attachList = attachRepository.findAllById(attachIds);

        //校验失败或错误的条目IDS
        var errorEntries = new ArrayList<Long>();

        //校验成功的条目
        var successEntries = new ArrayList<Long>();

        for (var entryPo : entryPos) {

            AttachPo attach = null;

            for (var item : attachList) {
                if (entryPo.getAttach() != null && Objects.equals(item.getId(), entryPo.getAttach().getId())) {
                    attach = item;
                    break;
                }
            }

            //文件附件不存在
            if (attach == null) {
                errorEntries.add(entryPo.getId());
                log.info("文件附件不存在 条目ID:{} 名称:{}", entryPo.getId(), entryPo.getName());
                continue;
            }

            //文件附件校验通过
            if (attach.getStatus() == 3) {
                successEntries.add(entryPo.getId());
                continue;
            }

            //文件附件校验中
            if (attach.getStatus() == 2) {
                continue;
            }

            //其他异常状态
            errorEntries.add(entryPo.getId());
            log.info("文件附件状态异常 条目ID:{} 名称:{} 状态:{}", entryPo.getId(), entryPo.getName(), attach.getStatus());
        }

        //更新云盘条目状态
        if (!errorEntries.isEmpty()) {
            var count = repository.removeEntryByEntryIds(errorEntries);
            if (count > 0) {
                log.info("已有 {} 个条云盘条目因文件附件不存在或校验失败被删除", errorEntries.size());
            }
        }
        if (!successEntries.isEmpty()) {
            var count = repository.updateEntryAttachStatusByEntryIds(successEntries, 3);
            if (count > 0) {
                log.info("已有 {} 个条云盘条目校验通过", successEntries.size());
            }
        }
    }


}