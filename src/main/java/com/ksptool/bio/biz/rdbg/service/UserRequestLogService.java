package com.ksptool.bio.biz.rdbg.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.rdbg.commons.model.HttpHeaderVo;
import com.ksptool.bio.biz.rdbg.model.userrequestlog.*;
import com.ksptool.bio.biz.rdbg.repository.UserRequestLogRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class UserRequestLogService {

    @Autowired
    private UserRequestLogRepository repository;

    @Autowired
    private Gson gson;

    public PageResult<GetUserRequestLogListVo> getUserRequestLogList(GetUserRequestLogListDto dto) {
        Page<GetUserRequestLogListVo> pVos = repository.getUserRequestLogList(dto.getUserRequestId(), dto.pageRequest());
        return PageResult.success(pVos.getContent(), pVos.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addUserRequestLog(AddUserRequestLogDto dto) {
        UserRequestLogPo insertPo = as(dto, UserRequestLogPo.class);
        repository.save(insertPo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void editUserRequestLog(EditUserRequestLogDto dto) throws BizException {
        UserRequestLogPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    public GetUserRequestLogDetailsVo getUserRequestLogDetails(CommonIdDto dto) throws BizException {
        UserRequestLogPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在."));

        GetUserRequestLogDetailsVo vo = new GetUserRequestLogDetailsVo();
        vo.setId(po.getId());
        vo.setRequestId(po.getRequestId());
        vo.setMethod(po.getMethod());
        vo.setUrl(po.getUrl());
        vo.setSource(po.getSource());
        vo.setRequestHeaders(null);
        vo.setRequestBodyLength(po.getRequestBodyLength());
        vo.setRequestBodyType(po.getRequestBodyType());
        vo.setRequestBody(po.getRequestBody());
        vo.setResponseHeaders(null);
        vo.setResponseBodyLength(po.getResponseBodyLength());
        vo.setResponseBodyType(po.getResponseBodyType());
        vo.setResponseBody(po.getResponseBody());
        vo.setStatusCode(po.getStatusCode());
        vo.setRedirectUrl(po.getRedirectUrl());
        vo.setStatus(po.getStatus());
        vo.setRequestTime(po.getRequestTime());
        vo.setResponseTime(po.getResponseTime());
        vo.setResponseTime(po.getResponseTime());

        //处理请求头、响应头
        List<HttpHeaderVo> requestHeaders = new ArrayList<>();
        List<HttpHeaderVo> responseHeaders = new ArrayList<>();

        if (StringUtils.isNotBlank(po.getRequestHeaders())) {
            requestHeaders = gson.fromJson(po.getRequestHeaders(), new TypeToken<List<HttpHeaderVo>>() {
            }.getType());
        }
        if (StringUtils.isNotBlank(po.getResponseHeaders())) {
            responseHeaders = gson.fromJson(po.getResponseHeaders(), new TypeToken<List<HttpHeaderVo>>() {
            }.getType());
        }

        vo.setRequestHeaders(requestHeaders);
        vo.setResponseHeaders(responseHeaders);
        return vo;
    }

    public GetUserRequestLogDetailsVo getLastUserRequestLogDetails(CommonIdDto dto) {

        UserRequestLogPo po = repository.getLastUserRequestLog(dto.getId());

        if (po == null) {
            return null;
        }

        GetUserRequestLogDetailsVo vo = new GetUserRequestLogDetailsVo();
        vo.setId(po.getId());
        vo.setRequestId(po.getRequestId());
        vo.setMethod(po.getMethod());
        vo.setUrl(po.getUrl());
        vo.setSource(po.getSource());
        vo.setRequestHeaders(null);
        vo.setRequestBodyLength(po.getRequestBodyLength());
        vo.setRequestBodyType(po.getRequestBodyType());
        vo.setRequestBody(po.getRequestBody());
        vo.setResponseHeaders(null);
        vo.setResponseBodyLength(po.getResponseBodyLength());
        vo.setResponseBodyType(po.getResponseBodyType());
        vo.setResponseBody(po.getResponseBody());
        vo.setStatusCode(po.getStatusCode());
        vo.setRedirectUrl(po.getRedirectUrl());
        vo.setStatus(po.getStatus());
        vo.setRequestTime(po.getRequestTime());
        vo.setResponseTime(po.getResponseTime());
        vo.setResponseTime(po.getResponseTime());

        //处理请求头、响应头
        List<HttpHeaderVo> requestHeaders = new ArrayList<>();
        List<HttpHeaderVo> responseHeaders = new ArrayList<>();

        if (StringUtils.isNotBlank(po.getRequestHeaders())) {
            requestHeaders = gson.fromJson(po.getRequestHeaders(), new TypeToken<List<HttpHeaderVo>>() {
            }.getType());
        }
        if (StringUtils.isNotBlank(po.getResponseHeaders())) {
            responseHeaders = gson.fromJson(po.getResponseHeaders(), new TypeToken<List<HttpHeaderVo>>() {
            }.getType());
        }

        vo.setRequestHeaders(requestHeaders);
        vo.setResponseHeaders(responseHeaders);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeUserRequestLog(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
        }
        if (!dto.isBatch()) {
            repository.deleteById(dto.getId());
        }
    }

}