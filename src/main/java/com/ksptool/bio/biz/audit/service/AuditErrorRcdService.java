package com.ksptool.bio.biz.audit.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.audit.model.auditerrorrcd.AuditErrorRcdPo;
import com.ksptool.bio.biz.audit.model.auditerrorrcd.dto.GetAuditErrorRcdListDto;
import com.ksptool.bio.biz.audit.model.auditerrorrcd.vo.GetAuditErrorRcdDetailsVo;
import com.ksptool.bio.biz.audit.model.auditerrorrcd.vo.GetAuditErrorRcdListVo;
import com.ksptool.bio.biz.audit.repository.AuditErrorRcdRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Slf4j
@Service
public class AuditErrorRcdService {

    private final Map<String, AtomicLong> errorCodeMap = new ConcurrentHashMap<>();
    @Autowired
    private AuditErrorRcdRepository repository;

    /**
     * 获取下一个错误代码
     *
     * @param category 错误分类
     * @return 错误代码
     */
    public String nextErrorCode(String category) {

        AtomicLong errorCode = errorCodeMap.computeIfAbsent(category, k -> {
            return new AtomicLong(0);
        });

        return "ERR-" + category + "-" + errorCode.incrementAndGet();
    }

    /**
     * 查询系统错误记录列表
     *
     * @param dto 查询条件
     * @return 系统错误记录列表
     */
    public PageResult<GetAuditErrorRcdListVo> getAuditErrorRcdList(GetAuditErrorRcdListDto dto) {
        AuditErrorRcdPo query = new AuditErrorRcdPo();
        assign(dto, query);

        Page<AuditErrorRcdPo> page = repository.getAuditErrorRcdList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAuditErrorRcdListVo> vos = as(page.getContent(), GetAuditErrorRcdListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 异步新增系统错误记录
     *
     * @param dto 新增系统错误记录
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void addAuditErrorRcdAsync(String errorCode, String requestUri, Long userId, String userName, Exception e) {
        try {

            AuditErrorRcdPo po = new AuditErrorRcdPo();
            po.setErrorCode(errorCode);
            po.setRequestUri(requestUri);
            po.setUserId(userId);
            po.setUserName(userName);
            po.setErrorType(e.getClass().getName());
            po.setErrorMessage(e.getMessage());
            po.setErrorStackTrace(ExceptionUtils.getStackTrace(e));
            repository.save(po);

        } catch (Exception ex) {
            log.error("新增系统错误记录失败: {}", e.getMessage());
        }
    }

    /**
     * 查询系统错误记录详情
     *
     * @param dto 查询条件
     * @return 系统错误记录详情
     */
    public GetAuditErrorRcdDetailsVo getAuditErrorRcdDetails(CommonIdDto dto) throws BizException {
        AuditErrorRcdPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAuditErrorRcdDetailsVo.class);
    }

    /**
     * 删除系统错误记录
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAuditErrorRcd(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

}