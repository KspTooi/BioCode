package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.core.common.IdsDiff;
import com.ksptool.bio.biz.core.model.pack.MenuPackPo;
import com.ksptool.bio.biz.core.model.pack.PackPo;
import com.ksptool.bio.biz.core.model.pack.dto.AddPackDto;
import com.ksptool.bio.biz.core.model.pack.dto.EditPackDto;
import com.ksptool.bio.biz.core.model.pack.dto.GetPackListDto;
import com.ksptool.bio.biz.core.model.pack.dto.UpdatePackMenuDto;
import com.ksptool.bio.biz.core.model.pack.vo.GetPackDetailsVo;
import com.ksptool.bio.biz.core.model.pack.vo.GetPackListVo;
import com.ksptool.bio.biz.core.repository.MenuPackRepository;
import com.ksptool.bio.biz.core.repository.PackRepository;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.bio.biz.core.common.TupleMapper.tupleAs;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Service
public class PackService {

    @Autowired
    private PackRepository repository;

    @Autowired
    private MenuPackRepository menuPackRepository;

    /**
     * 查询菜单包列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetPackListVo> getPackList(GetPackListDto dto) {
        Page<Tuple> page = repository.getPackList(dto, dto.pageRequest());
        return PageResult.success(tupleAs(page.getContent(), GetPackListVo.class), (int) page.getTotalElements());
    }

    /**
     * 新增菜单包
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPack(AddPackDto dto) throws BizException {
        if (repository.countByCode(dto.getCode()) > 0) {
            throw new BizException("菜单包编码已存在:[" + dto.getCode() + "]");
        }

        PackPo insertPo = as(dto, PackPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑菜单包
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editPack(EditPackDto dto) throws BizException {
        PackPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询菜单包详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetPackDetailsVo getPackDetails(CommonIdDto dto) throws BizException {
        PackPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        GetPackDetailsVo vo = as(po, GetPackDetailsVo.class);
        vo.setMenuIds(menuPackRepository.getMidsByPid(po.getId()));
        return vo;
    }

    /**
     * 删除菜单包
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePack(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            for (Long id : dto.getIds()) {
                menuPackRepository.removeByPid(id);
            }
            repository.deleteAllById(dto.getIds());
            return;
        }
        menuPackRepository.removeByPid(dto.getId());
        repository.deleteById(dto.getId());
    }


    /**
     * 根据菜单ID查询所属的菜单包列表
     *
     * @param menuId 菜单ID
     * @return 菜单包列表(仅含id和name)
     */
    public List<GetPackListVo> getPacksByMenuId(Long menuId) {
        var packIds = menuPackRepository.getPidsByMid(menuId);
        if (packIds.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(packIds).stream().map(p -> {
            var vo = new GetPackListVo();
            vo.setId(p.getId());
            vo.setName(p.getName());
            return vo;
        }).toList();
    }

    /**
     * 更新菜单包的菜单绑定
     *
     * @param dto 更新条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePackMenu(UpdatePackMenuDto dto) throws BizException {
        repository.findById(dto.getPackId())
                .orElseThrow(() -> new BizException("菜单包不存在或无权限访问."));

        var mpIdsDiff = new IdsDiff(menuPackRepository.getMidsByPid(dto.getPackId()), dto.getMenuIds());

        if (mpIdsDiff.hasAdd()) {
            var mpPos = mpIdsDiff.getAddIds().stream()
                    .map(menuId -> new MenuPackPo(menuId, dto.getPackId())).toList();
            menuPackRepository.saveAll(mpPos);
        }

        if (mpIdsDiff.hasRemove()) {
            menuPackRepository.removeByPidAndMids(dto.getPackId(), mpIdsDiff.getRemoveIds());
        }
    }

}
