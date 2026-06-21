package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.AacpAppModelPo;
import com.ksptool.bio.biz.aacp.model.aacpapp.AacpAppPo;
import com.ksptool.bio.biz.aacp.model.aacpapp.dto.AddAacpAppDto;
import com.ksptool.bio.biz.aacp.model.aacpapp.dto.EditAacpAppDto;
import com.ksptool.bio.biz.aacp.model.aacpapp.dto.GetAacpAppListDto;
import com.ksptool.bio.biz.aacp.model.aacpapp.vo.GetAacpAppDetailsVo;
import com.ksptool.bio.biz.aacp.model.aacpapp.vo.GetAacpAppListVo;
import com.ksptool.bio.biz.aacp.repository.AacpAppRepository;
import com.ksptool.bio.biz.aacp.repository.AppModelRepository;
import com.ksptool.bio.biz.core.common.IdsDiff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Service
public class AacpAppService {

    @Autowired
    private AacpAppRepository repository;

    @Autowired
    private AppModelRepository aamRepository;

    /**
     * 查询AACP应用列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAacpAppListVo> getAacpAppList(GetAacpAppListDto dto) {
        AacpAppPo query = new AacpAppPo();
        assign(dto, query);

        Page<AacpAppPo> page = repository.getAacpAppList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAacpAppListVo> vos = as(page.getContent(), GetAacpAppListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增AACP应用
     *
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAacpApp(AddAacpAppDto dto) throws BizException {

        if(repository.countAppByCodeExcludeId(dto.getCode(), null) > 0){
            throw new BizException("应用代码已被占用,请重新输入");
        }

        AacpAppPo insertPo = as(dto, AacpAppPo.class);
        insertPo.setAppKey(UUID.randomUUID().toString());
        repository.save(insertPo);

        var mids = dto.getModelIds();
        if (mids != null && !mids.isEmpty()) {
            var pos = mids.stream()
                    .map(mid -> new AacpAppModelPo(insertPo.getId(), mid)).toList();
            aamRepository.saveAll(pos);
        }
    }

    /**
     * 编辑AACP应用
     *
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAacpApp(EditAacpAppDto dto) throws BizException {
        AacpAppPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        if(repository.countAppByCodeExcludeId(dto.getCode(), dto.getId()) > 0){
            throw new BizException("应用代码已被占用,请重新输入");
        }

        assign(dto, updatePo);
        repository.save(updatePo);

        List<Long> existIds = aamRepository.getModelIdsByAppId(dto.getId());
        var idsDiff = new IdsDiff(existIds, dto.getModelIds());

        if (idsDiff.hasAdd()) {
            var toAdd = idsDiff.getAddIds().stream()
                    .map(mid -> new AacpAppModelPo(dto.getId(), mid)).toList();
            aamRepository.saveAll(toAdd);
        }

        if (idsDiff.hasRemove()) {
            aamRepository.removeByAppIdAndModelIds(dto.getId(), idsDiff.getRemoveIds());
        }
    }

    /**
     * 查询AACP应用详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAacpAppDetailsVo getAacpAppDetails(CommonIdDto dto) throws BizException {
        AacpAppPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        GetAacpAppDetailsVo vo = as(po, GetAacpAppDetailsVo.class);
        vo.setModelIds(aamRepository.getModelIdsByAppId(dto.getId()));
        return vo;
    }

    /**
     * 删除AACP应用
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAacpApp(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        aamRepository.removeByAppId(dto.getId());
        repository.deleteById(dto.getId());
    }

}