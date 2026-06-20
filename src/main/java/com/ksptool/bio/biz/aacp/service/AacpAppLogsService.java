package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.aacp.model.applogs.AacpAppLogsPo;
import com.ksptool.bio.biz.aacp.model.applogs.dto.GetAppLogsListDto;
import com.ksptool.bio.biz.aacp.model.applogs.vo.GetAppLogsDetailsVo;
import com.ksptool.bio.biz.aacp.model.applogs.vo.GetAppLogsListVo;
import com.ksptool.bio.biz.aacp.repository.AppLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Service
public class AacpAppLogsService {

    @Autowired
    private AppLogsRepository repository;

    /**
     * 查询模型调用记录列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAppLogsListVo> getAppLogsList(GetAppLogsListDto dto) {
        AacpAppLogsPo query = new AacpAppLogsPo();
        assign(dto, query);

        Page<AacpAppLogsPo> page = repository.getAppLogsList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAppLogsListVo> vos = as(page.getContent(), GetAppLogsListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 查询模型调用记录详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAppLogsDetailsVo getAppLogsDetails(CommonIdDto dto) throws BizException {
        AacpAppLogsPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAppLogsDetailsVo.class);
    }

    /**
     * 删除模型调用记录
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAppLogs(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}
