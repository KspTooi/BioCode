package com.ksptool.bio.biz.rdbg.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.bio.biz.auth.service.SessionService;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.rdbg.commons.model.HttpHeaderVo;
import com.ksptool.bio.biz.rdbg.commons.model.RequestSchema;
import com.ksptool.bio.biz.rdbg.model.userrequest.EditUserRequestDto;
import com.ksptool.bio.biz.rdbg.model.userrequest.GetUserRequestDetailsVo;
import com.ksptool.bio.biz.rdbg.model.userrequest.SaveAsUserRequestDto;
import com.ksptool.bio.biz.rdbg.model.userrequest.UserRequestPo;
import com.ksptool.bio.biz.rdbg.model.userrequestlog.UserRequestLogPo;
import com.ksptool.bio.biz.rdbg.model.userrequesttree.UserRequestTreePo;
import com.ksptool.bio.biz.rdbg.repository.UserRequestGroupRepository;
import com.ksptool.bio.biz.rdbg.repository.UserRequestLogRepository;
import com.ksptool.bio.biz.rdbg.repository.UserRequestRepository;
import com.ksptool.bio.biz.rdbg.repository.UserRequestTreeRepository;
import com.ksptool.bio.biz.relay.model.request.RequestPo;
import com.ksptool.bio.biz.relay.repository.RequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ksptool.bio.biz.auth.service.SessionService.session;
import static com.ksptool.entities.Entities.as;

@Slf4j
@Service
public class UserRequestService {

    @Autowired
    private UserRequestRepository repository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRequestTreeRepository userRequestTreeRepository;

    @Autowired
    private UserRequestGroupRepository userRequestGroupRepository;

    @Autowired
    private Gson gson;

    @Autowired
    private UserRequestLogRepository userRequestLogRepository;

    @Autowired
    private UserRequestFilterService userRequestFilterService;


    /**
     * 保存原始请求为用户请求
     *
     * @param dto 保存请求参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAsUserRequest(SaveAsUserRequestDto dto) throws BizException, AuthException {

        //查询原始请求
        RequestPo requestPo = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new BizException("原始请求不存在."));

        //创建用户请求
        UserRequestPo userRequestPo = new UserRequestPo();
        userRequestPo.setGroup(null);
        userRequestPo.setOriginalRequest(requestPo);
        userRequestPo.setUser(sessionService.requireUser());
        userRequestPo.setName(requestPo.getRequestId());//如果未提供名称 则使用请求ID作为名称
        userRequestPo.setMethod(requestPo.getMethod());
        userRequestPo.setUrl(requestPo.getUrl());

        //将请求头转换为列表
        List<HttpHeaderVo> requestHeaders = new ArrayList<>();

        try {
            Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> rhm = gson.fromJson(requestPo.getRequestHeaders(), mapType);
            for (Map.Entry<String, String> entry : rhm.entrySet()) {
                HttpHeaderVo httpHeaderVo = new HttpHeaderVo();
                httpHeaderVo.setK(entry.getKey());
                httpHeaderVo.setV(entry.getValue());
                requestHeaders.add(httpHeaderVo);
            }
        } catch (Exception e) {
        }

        userRequestPo.setRequestHeaders(gson.toJson(requestHeaders));
        userRequestPo.setRequestBodyType(requestPo.getRequestBodyType());
        userRequestPo.setRequestBody(requestPo.getRequestBody());

        if (StringUtils.isNotBlank(dto.getName())) {
            userRequestPo.setName(dto.getName());
        }

        //创建用户请求树
        UserRequestTreePo treePo = new UserRequestTreePo();
        treePo.setUser(sessionService.requireUser());
        treePo.setParent(null);
        treePo.setName(userRequestPo.getName());
        treePo.setKind(1); //0:请求组 1:用户请求
        treePo.setSeq(userRequestTreeRepository.getMinSeqInParent(null)); //新建的请求排在最前 
        treePo.setRequest(userRequestPo);

        userRequestPo.setTree(treePo);
        repository.save(userRequestPo);
    }


    @Transactional(rollbackFor = Exception.class)
    public void editUserRequest(EditUserRequestDto dto) throws BizException, AuthException {

        UserRequestPo updatePo = repository.getByIdAndUserId(dto.getId(), session().getUserId());

        if (updatePo == null) {
            throw new BizException("数据不存在或无权限操作.");
        }

        //如果请求有更名 同步修改树
        if (!updatePo.getName().equals(dto.getName())) {
            UserRequestTreePo treePo = updatePo.getTree();
            treePo.setName(dto.getName());
        }

        updatePo.setName(dto.getName());
        updatePo.setMethod(dto.getMethod());
        updatePo.setUrl(dto.getUrl());
        updatePo.setRequestHeaders(gson.toJson(dto.getRequestHeaders()));
        updatePo.setRequestBodyType(dto.getRequestBodyType());
        updatePo.setRequestBody(dto.getRequestBody());

        //级联修改
        repository.save(updatePo);
    }


    public GetUserRequestDetailsVo getUserRequestDetails(CommonIdDto dto) throws BizException {
        UserRequestPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询失败,数据不存在."));

        GetUserRequestDetailsVo vo = as(po, GetUserRequestDetailsVo.class);

        if (po.getRequestHeaders() != null) {
            Type listType = new TypeToken<List<HttpHeaderVo>>() {
            }.getType();
            vo.setRequestHeaders(gson.fromJson(po.getRequestHeaders(), listType));
        }

        return vo;
    }


    public void sendUserRequest(CommonIdDto dto) throws BizException, AuthException {

        UserPo userPo = sessionService.requireUser();
        UserRequestPo userRequestPo = repository.getByIdAndUserId(dto.getId(), userPo.getId());

        if (userRequestPo == null) {
            throw new BizException("数据不存在或无权限操作.");
        }

        //创建请求Schema
        RequestSchema requestSchema = new RequestSchema();
        requestSchema.setUrl(userRequestPo.getUrl());
        requestSchema.setMethod(userRequestPo.getMethod());
        requestSchema.parserHeaderFromJson(userRequestPo.getRequestHeaders());
        requestSchema.setBody(userRequestPo.getRequestBody().getBytes(StandardCharsets.UTF_8));
        requestSchema.setContentType(userRequestPo.getRequestBodyType());

        if (!requestSchema.getContentType().contains("application/json")) {
            throw new BizException("只能发送application/json请求");
        }

        HttpClient client = HttpClient.newHttpClient();

        //执行请求过滤器
        userRequestFilterService.doRequestFilter(requestSchema, userRequestPo);

        //创建重放记录
        UserRequestLogPo userRequestLogPo = new UserRequestLogPo();
        userRequestLogPo.setUserRequest(userRequestPo);
        userRequestLogPo.setRequestId(UUID.randomUUID().toString());
        userRequestLogPo.setMethod(requestSchema.getMethod());
        userRequestLogPo.setUrl(requestSchema.getUrl());
        userRequestLogPo.setSource("EAS服务");
        userRequestLogPo.setRequestHeaders(gson.toJson(requestSchema.getHeaders()));
        userRequestLogPo.setRequestBodyLength(requestSchema.getBody().length);
        userRequestLogPo.setRequestBodyType(requestSchema.getContentType());
        userRequestLogPo.setRequestBody(new String(requestSchema.getBody(), StandardCharsets.UTF_8));
        userRequestLogPo.setResponseHeaders("[]");
        userRequestLogPo.setResponseBodyLength(0);
        userRequestLogPo.setResponseBodyType("UNKNOW");
        userRequestLogPo.setResponseBody("{}");
        userRequestLogPo.setStatusCode(-1); //-1为请求失败
        userRequestLogPo.setRedirectUrl(null);
        userRequestLogPo.setStatus(3); //0:正常 1:HTTP失败 2:业务失败 3:连接超时
        userRequestLogPo.setRequestTime(LocalDateTime.now());
        userRequestLogPo.setResponseTime(null);


        try {
            //发送请求
            HttpResponse<String> response = client.send(requestSchema.getBuilder().build(), HttpResponse.BodyHandlers.ofString());

            //解析响应
            String responseBody = response.body();
            userRequestLogPo.setResponseBodyLength(responseBody.length());
            userRequestLogPo.setResponseBodyType(response.headers().firstValue("Content-Type").orElse(""));
            userRequestLogPo.setResponseBody(responseBody);
            userRequestLogPo.setStatusCode(response.statusCode());
            userRequestLogPo.setRedirectUrl(response.headers().firstValue("Location").orElse(null));
            userRequestLogPo.setStatus(0);
            userRequestLogPo.setResponseTime(LocalDateTime.now());


            //处理响应头
            List<HttpHeaderVo> responseHeadersList = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
                HttpHeaderVo responseHeaderVo = new HttpHeaderVo();
                responseHeaderVo.setK(entry.getKey());
                responseHeaderVo.setV(entry.getValue().get(0));
                responseHeadersList.add(responseHeaderVo);
            }

            //处理响应过滤器
            userRequestFilterService.doResponseFilter(response, userRequestPo, userRequestLogPo);

        } catch (IOException | InterruptedException e) {
            userRequestLogRepository.save(userRequestLogPo);
            throw new BizException("发送请求失败", e);
        } finally {
            //保存用户请求记录
            userRequestLogRepository.save(userRequestLogPo);
        }

    }

}