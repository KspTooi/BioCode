package com.ksptool.bio.biz.rdbg.service;

import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.auth.service.AuthService;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.repository.UserRepository;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.UserRequestEnvPo;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.dto.AddUserRequestEnvDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.dto.EditUserRequestEnvDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.dto.GetUserRequestEnvListDto;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.vo.GetUserRequestEnvDetailsVo;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.vo.GetUserRequestEnvListVo;
import com.ksptool.bio.biz.rdbg.repository.UserRequestEnvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class UserRequestEnvService {

    @Autowired
    private UserRequestEnvRepository repository;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    public PageResult<GetUserRequestEnvListVo> getUserRequestEnvList(GetUserRequestEnvListDto dto) throws AuthException {
        UserRequestEnvPo query = new UserRequestEnvPo();
        assign(dto, query);

        Page<GetUserRequestEnvListVo> page = repository.getUserRequestEnvList(query, SessionService.session().getUserId(), dto.pageRequest());
        return PageResult.success(page.getContent(), page.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addUserRequestEnv(AddUserRequestEnvDto dto) throws AuthException {
        UserRequestEnvPo insertPo = as(dto, UserRequestEnvPo.class);
        insertPo.setUser(sessionService.requireUser());
        repository.save(insertPo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editUserRequestEnv(EditUserRequestEnvDto dto) throws BizException {
        UserRequestEnvPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));
        assign(dto, updatePo);
        repository.save(updatePo);
    }

    public GetUserRequestEnvDetailsVo getUserRequestEnvDetails(CommonIdDto dto) throws BizException {
        UserRequestEnvPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));
        return as(po, GetUserRequestEnvDetailsVo.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeUserRequestEnv(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
        }
        if (!dto.isBatch()) {
            repository.deleteById(dto.getId());
        }
    }

    /**
     * 激活环境
     *
     * @param dto 环境ID
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateUserRequestEnv(CommonIdDto dto) throws Exception {
        Long userId = SessionService.session().getUserId();

        //查询环境
        UserRequestEnvPo env = repository.getByIdAndUserId(dto.getId(), userId);
        if (env == null) {
            throw new BizException("环境不存在或您无权访问此环境");
        }

        //更新用户已激活的环境
        UserPo user = sessionService.requireUser();

        //检查是否已重复激活相同环境
        //if (user.getActiveEnv() != null && user.getActiveEnv().getId().equals(env.getId())) {
        //    throw new BizException("您已激活此环境");
        //}

        //user.setActiveEnv(env);
        userRepository.save(user);
    }

}