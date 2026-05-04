package com.ksptool.bio.biz.package.service;

import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.exception.BizException;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import java.util.Optional;
import com.ksptool.bio.biz.package.repository.PackageRepository;
import com.ksptool.bio.biz.package.model.PackagePo;
import com.ksptool.bio.biz.package.model.vo.GetPackageListVo;
import com.ksptool.bio.biz.package.model.dto.GetPackageListDto;
import com.ksptool.bio.biz.package.model.vo.GetPackageDetailsVo;
import com.ksptool.bio.biz.package.model.dto.EditPackageDto;
import com.ksptool.bio.biz.package.model.dto.AddPackageDto;


@Service
public class PackageService {

    @Autowired
    private PackageRepository repository;

    /**
     * 查询菜单包列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetPackageListVo> getPackageList(GetPackageListDto dto){
        PackagePo query = new PackagePo();
        assign(dto,query);

        Page<PackagePo> page = repository.getPackageList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetPackageListVo> vos = as(page.getContent(), GetPackageListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增菜单包
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPackage(AddPackageDto dto){
        PackagePo insertPo = as(dto,PackagePo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑菜单包
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editPackage(EditPackageDto dto) throws BizException {
        PackagePo updatePo = repository.findById(dto.getId())
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
    public GetPackageDetailsVo getPackageDetails(CommonIdDto dto) throws BizException {
        PackagePo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetPackageDetailsVo.class);
    }

    /**
     * 删除菜单包
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removePackage(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
