package com.ksptool.bio.biz.core.service;

import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.exception.BizException;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

import com.ksptool.bio.biz.core.repository.PackRepository;
import com.ksptool.bio.biz.core.model.pack.PackPo;
import com.ksptool.bio.biz.core.model.pack.vo.GetPackListVo;
import com.ksptool.bio.biz.core.model.pack.dto.GetPackListDto;
import com.ksptool.bio.biz.core.model.pack.vo.GetPackDetailsVo;
import com.ksptool.bio.biz.core.model.pack.dto.EditPackDto;
import com.ksptool.bio.biz.core.model.pack.dto.AddPackDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;


@Service
public class PackService {

    @Autowired
    private PackRepository repository;

    /**
     * 查询菜单包列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetPackListVo> getPackList(GetPackListDto dto){
        PackPo query = new PackPo();
        assign(dto,query);

        Page<PackPo> page = repository.getPackList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetPackListVo> vos = as(page.getContent(), GetPackListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增菜单包
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPack(AddPackDto dto){
        PackPo insertPo = as(dto,PackPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑菜单包
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editPack(EditPackDto dto) throws BizException {
        PackPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询菜单包详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetPackDetailsVo getPackDetails(CommonIdDto dto) throws BizException {
        PackPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetPackDetailsVo.class);
    }

    /**
     * 删除菜单包
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePack(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
