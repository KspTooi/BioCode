package com.ksptool.bio.biz.core.service;


import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.model.org.OrgPo;
import com.ksptool.bio.biz.core.model.org.dto.AddOrgDto;
import com.ksptool.bio.biz.core.model.org.dto.EditOrgDto;
import com.ksptool.bio.biz.core.model.org.dto.GetOrgListDto;
import com.ksptool.bio.biz.core.model.org.dto.GetOrgTreeDto;
import com.ksptool.bio.biz.core.model.org.vo.GetOrgDetailsVo;
import com.ksptool.bio.biz.core.model.org.vo.GetOrgListVo;
import com.ksptool.bio.biz.core.model.org.vo.GetOrgTreeVo;
import com.ksptool.bio.biz.core.repository.OrgRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.5.2(B).1
 */
@Service
public class OrgService {

    @Autowired
    private OrgRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


    /**
     * 查询组织架构树
     *
     * @param dto
     * @return
     */
    public List<GetOrgTreeVo> getOrgTree(GetOrgTreeDto dto) {

        //全量查询组织 按排序排序
        List<OrgPo> pos;
        if (dto.getRootId() == null) {
            pos = repository.findAll(Sort.by(Sort.Direction.ASC, "seq"));
        } else {
            pos = repository.getAllByRootId(dto.getRootId());
        }

        List<GetOrgTreeVo> flatTreeVos = as(pos, GetOrgTreeVo.class);
        List<GetOrgTreeVo> treeVos = new ArrayList<>();

        if (flatTreeVos.isEmpty()) {
            return treeVos;
        }

        Map<Long, GetOrgTreeVo> voMap = new HashMap<>();
        for (GetOrgTreeVo vo : flatTreeVos) {
            vo.setChildren(new ArrayList<>());
            voMap.put(vo.getId(), vo);
        }

        for (GetOrgTreeVo vo : flatTreeVos) {
            if (vo.getParentId() == null) {
                treeVos.add(vo);
                continue;
            }

            GetOrgTreeVo parentVo = voMap.get(vo.getParentId());
            if (parentVo == null) {
                continue;
            }

            if (parentVo.getChildren() == null) {
                parentVo.setChildren(new ArrayList<>());
            }
            parentVo.getChildren().add(vo);
        }
        return treeVos;
    }

    /**
     * 获取组织机构列表
     *
     * @param dto
     * @return
     */
    public PageResult<GetOrgListVo> getOrgList(GetOrgListDto dto) {
        OrgPo query = new OrgPo();
        assign(dto, query);

        Page<OrgPo> page = repository.getOrgList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetOrgListVo> vos = as(page.getContent(), GetOrgListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增组织机构
     *
     * @param dto 新增组织机构参数
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void addOrg(AddOrgDto dto) throws BizException {

        //先处理顶级企业新增
        if (dto.getKind() == 0) {

            //先校验名字是否唯一
            if (repository.countByNameAndParentIdExcludeId(dto.getName(), null, null) > 0) {
                throw new BizException("无法处理新增请求,企业名称 [" + dto.getName() + "] 已存在.");
            }

            var addPo = as(dto, OrgPo.class);
            addPo.setTopId(-1L);
            addPo.setOrgId(-1L);
            addPo.setParentId(null);
            addPo.setOrgPathIds("-1L");
            addPo.setLevel(1);

            //先保存以获取回填的ID
            addPo = repository.saveAndFlush(addPo);

            //后处理
            addPo.setTopId(addPo.getId());
            addPo.setOrgId(addPo.getId());
            addPo.setOrgPathIds(addPo.getId().toString());
            repository.saveAndFlush(addPo);

            //组织机构树发生变动后，给该租户下全部在线用户加版本(用户下一次请求时重新预计算权限)
            userService.increaseDvByRootId(addPo.getRootId());
            return;
        }

        //子企业或部门新增
        var parentPo = repository.findById(dto.getParentId())
                .orElseThrow(() -> new BizException("未能找到上级组织 ID: " + dto.getParentId()));

        //校验层级是否超过限制
        if (parentPo.getLevel() >= 16) {
            throw new BizException("组织架构层级超过限制! 最大层级为16");
        }

        //校验子机构名称是否唯一
        if (repository.countByNameAndParentIdExcludeId(dto.getName(), parentPo.getId(), null) > 0) {
            throw new BizException("无法处理新增请求,上级组织 [" + parentPo.getName() + "] 下已有同名子机构 [" + dto.getName() + "].");
        }

        //先合并同类项
        var addPo = as(dto, OrgPo.class);
        addPo.setTopId(parentPo.getTopId());
        addPo.setOrgId(parentPo.getTopId());
        addPo.setParentId(parentPo.getId());
        addPo.setOrgPathIds("-1L");
        addPo.setLevel(parentPo.getLevel() + 1);

        //先保存以获取回填的ID
        addPo = repository.saveAndFlush(addPo);

        //后处理 
        addPo.setOrgPathIds(parentPo.getOrgPathIds() + "," + addPo.getId().toString());

        //如果上级是子企业 直接配置直属企业为子企业
        if (parentPo.getKind() == 1) {
            addPo.setOrgId(parentPo.getId());
        }

        //如果上级不是子企业，继承上级的直属企业ID
        if (parentPo.getKind() != 1) {
            addPo.setOrgId(parentPo.getOrgId());
        }

        repository.saveAndFlush(addPo);

        //组织机构树发生变动后，给该租户下全部在线用户加版本(用户下一次请求时重新预计算权限)
        userService.increaseDvByRootId(addPo.getRootId());
    }

    /**
     * 编辑组织机构
     *
     * @param dto
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void editOrg(EditOrgDto dto) throws BizException {

        OrgPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));


        //先处理顶级企业编辑
        if (updatePo.getKind() == 0) {

            //先校验名字是否唯一
            if (repository.countByNameAndParentIdExcludeId(dto.getName(), null, updatePo.getId()) > 0) {
                throw new BizException("无法处理编辑请求,企业名称 [" + dto.getName() + "] 已存在.");
            }

            //合并同类项
            assign(dto, updatePo);
            repository.save(updatePo);

            //组织机构树发生变动后，给该租户下全部在线用户加版本(用户下一次请求时重新预计算权限)
            userService.increaseDvByRootId(updatePo.getRootId());
            return;
        }

        //子企业或部门编辑
        if (dto.getParentId() == null) {
            throw new BizException("无法处理编辑请求,未指定上级组织.");
        }

        //查询上级组织
        var parentPo = repository.findById(dto.getParentId())
                .orElseThrow(() -> new BizException("无法处理编辑请求,上级组织不存在. ID: " + dto.getParentId()));

        //上级组织不能是自身
        if (Objects.equals(updatePo.getId(), parentPo.getId())) {
            throw new BizException("无法处理编辑请求,上级组织不能是自身.");
        }

        //校验名称是否唯一
        if (repository.countByNameAndParentIdExcludeId(dto.getName(), parentPo.getId(), updatePo.getId()) > 0) {
            throw new BizException("无法处理编辑请求,上级组织 [" + parentPo.getName() + "] 下已有同名子机构 [" + dto.getName() + "].");
        }

        //校验层级是否超过限制
        if (parentPo.getLevel() >= 16) {
            throw new BizException("组织架构层级超过限制! 最大层级为16");
        }

        //校验上级组织不能是当前组织的子孙节点
        var currentIdFlag = "," + updatePo.getId() + ",";
        var parentPathIds = "," + parentPo.getOrgPathIds() + ",";
        if (parentPathIds.contains(currentIdFlag)) {
            throw new BizException("无法处理编辑请求,父级组织不能是当前组织的子孙节点.");
        }

        //处理子机构自身路径 先向上找直到Root为止 以此构建一个BasePathIds
        var basePathIds = new ArrayList<String>();
        basePathIds.add(updatePo.getId().toString());

        var parentId = parentPo.getId();

        while (parentId != null) {
            var parent = repository.findById(parentId)
                    .orElseThrow(() -> new BizException("无法处理编辑请求,父级组织不存在."));

            basePathIds.add(parent.getId().toString());

            //如果父级是NULL，则停止向上查找
            if (parent.getParentId() == null) {
                break;
            }

            parentId = parent.getParentId();
        }
        //反转数组 因为向上查找时是反向的
        Collections.reverse(basePathIds);

        updatePo.setOrgPathIds(String.join(",", basePathIds));

        //递归处理子孙机构 需要重建这个机构以及这个机构下的全部子孙机构的组织路径ID(orgPathIds) 这里为了安全起见直接通过递归重建这个企业下全部子孙机构的orgPathIds
        var updatedPos = new ArrayList<OrgPo>();
        rebuildOrgPathIds(updatePo, basePathIds, updatedPos);

        //保存所有更新后的机构
        repository.saveAll(updatedPos);

        //合并同类项
        assign(dto, updatePo);
        updatePo.setLevel(parentPo.getLevel() + 1);

        //保存当前修改的机构
        repository.save(updatePo);

        //组织机构树发生变动后，给该租户下全部在线用户加版本(用户下一次请求时重新预计算权限)
        userService.increaseDvByRootId(updatePo.getRootId());
    }

    /**
     * 获取组织机构详情
     *
     * @param dto
     * @return
     * @throws BizException
     */
    public GetOrgDetailsVo getOrgDetails(CommonIdDto dto) throws BizException {
        OrgPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));
        return as(po, GetOrgDetailsVo.class);
    }

    /**
     * 删除组织机构
     *
     * @param dto
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeOrg(CommonIdDto dto) throws BizException {

        var ids = dto.toIds();

        //根据IDS查询
        List<OrgPo> pos = repository.findAllById(ids);

        for (var po : pos) {

            //查询是否有子组织
            if (repository.countByParentId(po.getId()) > 0) {
                throw new BizException("无法处理删除请求,该机构下仍有子机构,不能删除.");
            }

            //查询是否还有人员
            if (repository.countUserByDeptIds(Collections.singletonList(po.getId())) > 0) {
                throw new BizException("无法处理删除请求,该机构下仍有用户,请先移除用户后再删除.");
            }

            repository.delete(po);
        }

    }


    /**
     * 递归重建组织路径ID
     *
     * @param po          当前机构
     * @param basePathIds 基础路径ID列表
     * @param updatedPos  更新后的机构列表
     */
    public void rebuildOrgPathIds(OrgPo po, List<String> basePathIds, List<OrgPo> updatedPos) {

        var currentBasePath = String.join(",", basePathIds);

        //查询当前机构下的子机构
        var subtree = repository.getByParentId(po.getId());

        if (subtree.isEmpty()) {
            return;
        }

        //处理每个子机构
        for (var childPo : subtree) {

            //构建新的基础路径ID列表
            var newBasePathIds = new ArrayList<String>();
            newBasePathIds.addAll(basePathIds);
            newBasePathIds.add(childPo.getId().toString());
            rebuildOrgPathIds(childPo, newBasePathIds, updatedPos);

            childPo.setOrgPathIds(currentBasePath + "," + childPo.getId().toString());
            updatedPos.add(childPo);
        }

    }

}
