package com.ksptool.bio.biz.auth.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.model.basicpat.BasicPatPo;
import com.ksptool.bio.biz.auth.model.basicpat.dto.AddBasicPatDto;
import com.ksptool.bio.biz.auth.model.basicpat.dto.GetBasicPatListDto;
import com.ksptool.bio.biz.auth.model.basicpat.vo.GetBasicPatDetailsVo;
import com.ksptool.bio.biz.auth.model.basicpat.vo.GetBasicPatListVo;
import com.ksptool.bio.biz.auth.repository.BasicPatRepository;
import com.ksptool.bio.biz.auth.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * 基本PAT业务
 * 
 * @author KspTool
 * @since 1.7.5(E).1
 */
@Service
public class BasicPatService {

    @Autowired
    private BasicPatRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 查询基本PAT列表
     *
     * @param dto 查询条件
     * @return 查询结果
     */
    public PageResult<GetBasicPatListVo> getBasicPatList(GetBasicPatListDto dto) throws AuthException {
        BasicPatPo query = new BasicPatPo();
        assign(dto, query);
        query.setUserId(SessionService.session().getUserId());

        Page<BasicPatPo> page = repository.getBasicPatList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetBasicPatListVo> vos = as(page.getContent(), GetBasicPatListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增基本PAT，自动生成usk-xxx格式令牌
     *
     * @param dto 新增条件
     * @return 完整令牌明文，创建后仅展示一次
     */
    @Transactional(rollbackFor = Exception.class)
    public String addBasicPat(AddBasicPatDto dto) throws AuthException {
        BasicPatPo insertPo = as(dto, BasicPatPo.class);
        insertPo.setUserId(SessionService.session().getUserId());

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fullToken = "usk-" + uuid;
        insertPo.setPatPt(fullToken.substring(0, 9) + "***********************" + fullToken.substring(32));
        insertPo.setPatCt(passwordEncoder.encode(fullToken));

        repository.save(insertPo);
        return fullToken;
    }

    /**
     * 查询基本PAT详情
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws BizException 业务异常
     */
    public GetBasicPatDetailsVo getBasicPatDetails(CommonIdDto dto) throws Exception {
        BasicPatPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        if (!po.getUserId().equals(SessionService.session().getUserId())) {
            throw new BizException("无权查看其他用户的PAT详情");
        }
        return as(po, GetBasicPatDetailsVo.class);
    }

    /**
     * 删除基本PAT
     *
     * @param dto 删除条件
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeBasicPat(CommonIdDto dto) throws Exception {
        BasicPatPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("删除失败,数据不存在或无权限访问."));
        if (!po.getUserId().equals(SessionService.session().getUserId())) {
            throw new BizException("无权删除其他用户的PAT");
        }
        repository.deleteById(dto.getId());
    }
}
