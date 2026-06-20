package com.ksptool.bio.biz.aacp.service;

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
import com.ksptool.bio.biz.aacp.repository.AppLogsRepository;
import com.ksptool.bio.biz.aacp.model.applogs.AppLogsPo;
import com.ksptool.bio.biz.aacp.model.applogs.vo.GetAppLogsListVo;
import com.ksptool.bio.biz.aacp.model.applogs.dto.GetAppLogsListDto;
import com.ksptool.bio.biz.aacp.model.applogs.vo.GetAppLogsDetailsVo;
import com.ksptool.bio.biz.aacp.model.applogs.dto.EditAppLogsDto;
import com.ksptool.bio.biz.aacp.model.applogs.dto.AddAppLogsDto;


@Service
public class AppLogsService {

    @Autowired
    private AppLogsRepository repository;

    /**
     * 查询模型调用记录列表
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetAppLogsListVo> getAppLogsList(GetAppLogsListDto dto){
        AppLogsPo query = new AppLogsPo();
        assign(dto,query);

        Page<AppLogsPo> page = repository.getAppLogsList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAppLogsListVo> vos = as(page.getContent(), GetAppLogsListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增模型调用记录
     * @param dto 新增条件
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAppLogs(AddAppLogsDto dto){
        AppLogsPo insertPo = as(dto,AppLogsPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑模型调用记录
     * @param dto 编辑条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editAppLogs(EditAppLogsDto dto) throws BizException {
        AppLogsPo updatePo = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto,updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询模型调用记录详情
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetAppLogsDetailsVo getAppLogsDetails(CommonIdDto dto) throws BizException {
        AppLogsPo po = repository.findById(dto.getId())
            .orElseThrow(()-> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po,GetAppLogsDetailsVo.class);
    }

    /**
     * 删除模型调用记录
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
