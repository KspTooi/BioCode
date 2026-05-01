package com.ksptool.bio.biz.audit.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.audit.model.auditlogin.AuditLoginPo;
import com.ksptool.bio.biz.audit.model.auditlogin.dto.GetAuditLoginListDto;
import com.ksptool.bio.biz.audit.model.auditlogin.vo.GetAuditLoginDetailsVo;
import com.ksptool.bio.biz.audit.model.auditlogin.vo.GetAuditLoginListVo;
import com.ksptool.bio.biz.audit.repository.AuditLoginRepository;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


@Slf4j
@Service
public class AuditLoginService {

    @Autowired
    private AuditLoginRepository repository;

    /*
     * 获取登录日志列表
     * @param dto 查询条件
     * @return 登录日志列表
     */
    public PageResult<GetAuditLoginListVo> getAuditLoginList(GetAuditLoginListDto dto) {

        AuditLoginPo query = new AuditLoginPo();
        assign(dto, query);

        Page<AuditLoginPo> page = repository.getAuditLoginList(query, dto.pageRequest());

        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetAuditLoginListVo> vos = as(page.getContent(), GetAuditLoginListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }


    /*
     * 获取登录日志详情
     * @param dto 查询条件
     * @return 登录日志详情
     */
    public GetAuditLoginDetailsVo getAuditLoginDetails(CommonIdDto dto) throws BizException {
        AuditLoginPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAuditLoginDetailsVo.class);
    }

    /*
     * 删除登录日志
     * @param dto 删除条件
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeAuditLogin(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }


    /**
     * 异步记录登录审计日志
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param status   状态 0:成功 1:失败
     * @param message  消息
     * @param ipAddr   IP地址
     * @param uaString 用户代理字符串
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void recordLoginAudit(Long userId, String username, Integer status, String message, String ipAddr, String uaString) {
        try {

            var po = new AuditLoginPo();
            po.setUserId(userId);
            po.setUsername(username);
            po.setLoginKind(0); // 0:用户名密码
            po.setIpAddr("Unknown");
            po.setLocation("不支持");
            po.setBrowser("Unknown");
            po.setOs("Unknown");
            po.setStatus(status); // 0:成功 1:失败
            po.setMessage(message);

            UserAgent userAgent = UserAgent.parseUserAgentString(uaString);
            po.setIpAddr(ipAddr);
            po.setBrowser(userAgent.getBrowser().getName());
            po.setOs(userAgent.getOperatingSystem().getName());

            // 再次检查关键字段，防止获取到的值为 null 或空字符串
            if (StringUtils.isBlank(po.getIpAddr())) {
                po.setIpAddr("Unknown");
            }
            if (StringUtils.isBlank(po.getBrowser())) {
                po.setBrowser("Unknown");
            }
            if (StringUtils.isBlank(po.getOs())) {
                po.setOs("Unknown");
            }
            if (StringUtils.isBlank(po.getLocation())) {
                po.setLocation("Unknown");
            }

            repository.save(po);
        } catch (Exception e) {
            // 审计日志记录失败不应影响正常登录流程
            log.error("记录登录审计日志失败: username={}", username, e);
        }
    }


}