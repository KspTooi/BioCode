package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.common.RowScopes;
import com.ksptool.bio.biz.auth.model.GroupPermissionPo;
import com.ksptool.bio.biz.auth.model.UserGroupPo;
import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.repository.GroupPermissionRepository;
import com.ksptool.bio.biz.auth.repository.GroupRepository;
import com.ksptool.bio.biz.auth.repository.PermissionRepository;
import com.ksptool.bio.biz.auth.repository.UserGroupRepository;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.common.IdsDiff;
import com.ksptool.bio.biz.core.common.SuperEntities;
import com.ksptool.bio.biz.core.common.Switch;
import com.ksptool.bio.biz.core.model.pack.RootPackPo;
import com.ksptool.bio.biz.core.model.root.CoreRootPo;
import com.ksptool.bio.biz.core.model.root.dto.AddCoreRootDto;
import com.ksptool.bio.biz.core.model.root.dto.EditCoreRootDto;
import com.ksptool.bio.biz.core.model.root.dto.GetCoreRootListDto;
import com.ksptool.bio.biz.core.model.root.dto.UpdateRootRpDto;
import com.ksptool.bio.biz.core.model.root.vo.GetCoreRootDetailsVo;
import com.ksptool.bio.biz.core.model.root.vo.GetCoreRootListVo;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.CoreRootRepository;
import com.ksptool.bio.biz.core.repository.PackRepository;
import com.ksptool.bio.biz.core.repository.RootPackRepository;
import com.ksptool.bio.biz.core.repository.UserRepository;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.6.21(U).90
 */
@Service
public class CoreRootService {

    @Autowired
    private CoreRootRepository repository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserGroupRepository ugRepository;

    @Autowired
    private PermissionRepository pRepository;

    @Autowired
    private GroupPermissionRepository gpRepository;

    @Autowired
    private RootPackRepository rpRepository;

    @Autowired
    private PackRepository packRepository;

    /**
     * 查询租户列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetCoreRootListVo> getCoreRootList(GetCoreRootListDto dto) {
        Page<Tuple> page = repository.getCoreRootList(dto, dto.pageRequest());
        return PageResult.success(tupleAs(page.getContent(), GetCoreRootListVo.class), (int) page.getTotalElements());
    }

    /**
     * 新增租户
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCoreRoot(AddCoreRootDto dto) throws BizException {

        //查询名称是否被占用
        var existPo = repository.countByNameExcludeId(dto.getName(), null);

        if (existPo > 0) {
            throw new BizException("租户名称已存在:[" + dto.getName() + "]");
        }

        //查询管理员账号是否被占用
        var adminUser = userRepository.countByUsername(dto.getAdminUsername());

        if (adminUser > 0) {
            throw new BizException("无法创建用户，该用户名已被占用:[" + dto.getAdminUsername() + "]");
        }


        //先创建租户 这样才可以拿到租户ID
        CoreRootPo insertPo = as(dto, CoreRootPo.class);
        insertPo.setAdminUserId(-1L);
        insertPo.setIsSystem(Switch.no());
        insertPo = repository.save(insertPo);

        //给租户创建一个管理员账号
        var u = new UserPo();
        u.setUsername(dto.getAdminUsername());
        u.setPassword(passwordEncoder.encode(dto.getAdminPassword()));
        u.setNickname(dto.getName() + "-租户管理员");
        u.setGender(2);
        u.setPhone(null);
        u.setEmail(null);
        u.setLoginCount(0);
        u.setStatus(Switch.on());
        u.setRootId(insertPo.getId());
        u.setIsSystem(Switch.yes());
        u.setDataVersion(0L);
        u = userRepository.save(u);

        //修改租户 设置管理账号ID
        insertPo.setAdminUserId(u.getId());
        repository.save(insertPo);

        //为该租户创建固定角色
        var g = new GroupPo();
        g.setRootId(insertPo.getId());
        g.setOrgId(null);
        g.setCode("root_admin");
        g.setName("租户管理员");
        g.setRemark("自动创建的角色，该角色拥有本租户下的的全部权限。");
        g.setStatus(Switch.on());
        g.setSeq(0);
        g.setRowScope(RowScopes.ALL);
        g.setIsSystem(Switch.yes());
        groupRepository.save(g);

        //给新创建的管理员账号分配为租户管理员
        var ug = new UserGroupPo();
        ug.setUserId(u.getId());
        ug.setGroupId(g.getId());
        ugRepository.save(ug);

        //查找超级权限
        var superPermission = pRepository.getByCode(SuperEntities.PERMISSION.getCode());
        if (superPermission == null) {
            throw new BizException("创建租户时，超级权限不存在，请检查系统内置权限码是否完整!");
        }

        //给新创建的管理员账号分配超级权限
        var gp = new GroupPermissionPo();
        gp.setGroupId(g.getId());
        gp.setPermissionId(superPermission.getId());
        gpRepository.save(gp);
    }

    /**
     * 编辑租户
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editCoreRoot(EditCoreRootDto dto) throws BizException {

        //查询名称是否被占用
        var existPo = repository.countByNameExcludeId(dto.getName(), dto.getId());

        if (existPo > 0) {
            throw new BizException("租户名称已存在:[" + dto.getName() + "]");
        }

        CoreRootPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        //内置租户不可调整状态
        if (updatePo.isSystem() && dto.getStatus() != null && !dto.getStatus().equals(updatePo.getStatus())) {
            throw new BizException("内置租户不允许调整状态！");
        }

        //内置租户不可调整到期时间
        if (updatePo.isSystem() && dto.getExpireTime() != null && !dto.getExpireTime().equals(updatePo.getExpireTime())) {
            throw new BizException("内置租户不允许调整到期时间！");
        }

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询租户详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetCoreRootDetailsVo getCoreRootDetails(CommonIdDto dto) throws BizException {
        CoreRootPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        GetCoreRootDetailsVo vo = as(po, GetCoreRootDetailsVo.class);
        vo.setPackIds(rpRepository.getPidsByRid(po.getId()));
        return vo;
    }

    /**
     * 删除租户
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeCoreRoot(CommonIdDto dto) throws BizException {

        if (dto.isBatch()) {
            for (Long id : dto.getIds()) {
                var root = repository.findById(id)
                    .orElseThrow(() -> new BizException("租户不存在或无权限访问."));
                if (root.isSystem()) {
                    throw new BizException("内置租户不允许删除！");
                }
                rpRepository.removeByRid(id);
            }
            repository.deleteAllById(dto.getIds());
            return;
        }

        var root = repository.findById(dto.getId())
            .orElseThrow(() -> new BizException("租户不存在或无权限访问."));
        if (root.isSystem()) {
            throw new BizException("内置租户不允许删除！");
        }

        rpRepository.removeByRid(dto.getId());
        repository.deleteById(dto.getId());

        //销毁该租户下所有用户会话
        sessionService.closeSessionByRootId(dto.getId());
    }

    /**
     * 更新租户绑定的菜单包
     *
     * @param dto 更新条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRootRp(UpdateRootRpDto dto) throws BizException {
        var root = repository.findById(dto.getRootId())
                .orElseThrow(() -> new BizException("租户不存在或无权限访问."));

        if (root.isSystem()) {
            throw new BizException("内置租户不允许绑定菜单包！");
        }

        //校验菜单包ID是否存在
        var packIds = dto.getPackIds();
        if (!packIds.isEmpty()) {
            var existingCount = packRepository.countByIds(packIds);
            if (existingCount != packIds.size()) {
                throw new BizException("菜单包ID不存在");
            }
        }

        var rpIdsDiff = new IdsDiff(rpRepository.getPidsByRid(dto.getRootId()), packIds);

        if (rpIdsDiff.hasAdd()) {
            var rpPos = rpIdsDiff.getAddIds().stream()
                    .map(packId -> new RootPackPo(dto.getRootId(), packId)).toList();
            rpRepository.saveAll(rpPos);
        }

        if (rpIdsDiff.hasRemove()) {
            rpRepository.removeByRidAndPids(dto.getRootId(), rpIdsDiff.getRemoveIds());
        }
    }

}
