package com.ksptool.bio.biz.document.service;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.bio.biz.document.model.epdoc.EndpointDocPo;
import com.ksptool.bio.biz.document.model.epdocoperation.*;
import com.ksptool.bio.biz.document.model.epdocversion.EndpointDocVersionPo;
import com.ksptool.bio.biz.document.repository.EndpointDocOperationRepository;
import com.ksptool.bio.biz.document.repository.EndpointDocRepository;
import com.ksptool.bio.biz.document.repository.EndpointDocVersionRepository;
import com.ksptool.bio.biz.rdbg.commons.JsonRequestBodySchemaParser;
import com.ksptool.bio.biz.rdbg.commons.JsonResponseBodySchemaParser;
import com.ksptool.bio.biz.rdbg.commons.model.BodySchema;
import com.ksptool.bio.biz.relay.model.relayserver.RelayServerPo;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EpDocOperationService {

    @Autowired
    private EndpointDocOperationRepository endpointDocOperationRepository;

    @Autowired
    private EndpointDocVersionRepository endpointDocVersionRepository;

    @Autowired
    private EndpointDocRepository endpointDocRepository;

    @Autowired
    private Gson gson;


    /**
     * 获取端点文档接口标签列表
     *
     * @param dto 请求参数
     * @return 端点文档接口标签列表
     */
    public List<GetEpDocOperationTagListVo> getEpDocOperationTagList(GetEpDocOperationTagListDto dto) throws BizException {

        //查询端点文档
        EndpointDocPo epDocPo = endpointDocRepository.findById(dto.getEpDocId())
                .orElseThrow(() -> new BizException("端点文档不存在"));

        //查询端点文档版本
        EndpointDocVersionPo versionPo = null;

        //如果版本ID不为空，则查询指定版本
        if (dto.getEpDocVersionId() != null) {
            versionPo = endpointDocVersionRepository.findById(dto.getEpDocVersionId())
                    .orElseThrow(() -> new BizException("版本不存在"));
        }

        //如果版本ID为空，则查询最新版本
        if (versionPo == null) {

            versionPo = endpointDocVersionRepository.getLatestVersion(epDocPo.getId());

            if (versionPo == null) {
                throw new BizException("最新版本不存在");
            }

        }

        //查询版本下的全部接口PO
        List<EndpointDocOperationPo> operationPos = endpointDocOperationRepository.getOperationListByVersionId(versionPo.getId());
        if (operationPos == null || operationPos.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<GetEpDocOperationTagListOperationDefineVo>> tagToDefineList = new LinkedHashMap<>();

        for (EndpointDocOperationPo op : operationPos) {
            List<String> tagList = new ArrayList<>();
            String tag = op.getTag();

            if (StringUtils.isBlank(tag)) {
                tagList.add(null);
            }
            if (StringUtils.isNotBlank(tag)) {
                String[] parts = tag.split(",");
                for (String p : parts) {
                    String t = p == null ? null : p.trim();
                    if (StringUtils.isBlank(t)) {
                        continue;
                    }
                    tagList.add(t);
                }
                if (tagList.isEmpty()) {
                    tagList.add(null);
                }
            }

            GetEpDocOperationTagListOperationDefineVo defineVo = new GetEpDocOperationTagListOperationDefineVo();
            defineVo.setId(op.getId());
            if (StringUtils.isNotBlank(op.getSummary())) {
                defineVo.setName(op.getSummary());
            }
            if (StringUtils.isBlank(defineVo.getName())) {
                defineVo.setName(op.getOperationId());
            }
            defineVo.setMethod(op.getMethod());

            for (String t : tagList) {
                List<GetEpDocOperationTagListOperationDefineVo> list = tagToDefineList.computeIfAbsent(t, k -> new ArrayList<>());
                list.add(defineVo);
            }
        }

        List<GetEpDocOperationTagListVo> result = new ArrayList<>();
        for (Map.Entry<String, List<GetEpDocOperationTagListOperationDefineVo>> entry : tagToDefineList.entrySet()) {
            String t = entry.getKey();
            Long cnt = (long) entry.getValue().size();
            GetEpDocOperationTagListVo vo = new GetEpDocOperationTagListVo(t, cnt);
            vo.setOperationDefineList(entry.getValue());
            result.add(vo);
        }
        return result;
    }

    /**
     * 获取端点文档接口详情
     *
     * @param id 接口ID
     * @return 端点文档接口详情
     */
    public GetEpDocOperationDetailsVo getEpDocOperationDetails(Long id) throws BizException {

        EndpointDocOperationPo po = endpointDocOperationRepository.findById(id)
                .orElseThrow(() -> new BizException("接口不存在"));

        GetEpDocOperationDetailsVo vo = new GetEpDocOperationDetailsVo();
        vo.setId(po.getId());
        vo.setPath(po.getPath());
        vo.setMethod(po.getMethod());
        vo.setSummary(po.getSummary());
        vo.setDescription(po.getDescription());
        vo.setOperationId(po.getOperationId());

        if (StringUtils.isNotBlank(po.getReqBodyJson())) {
            BodySchema reqBody = gson.fromJson(po.getReqBodyJson(), BodySchema.class);
            vo.setReqBody(reqBody);
        }

        if (StringUtils.isNotBlank(po.getResBodyJson())) {
            BodySchema resBody = gson.fromJson(po.getResBodyJson(), BodySchema.class);
            vo.setResBody(resBody);
        }

        return vo;
    }


    /**
     * 保存端点文档接口
     *
     * @param openAPI   端点文档
     * @param versionId 端点文档版本ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOperation(OpenAPI openAPI, Long versionId) throws BizException {

        //查询版本
        EndpointDocVersionPo versionPo = endpointDocVersionRepository.findById(versionId)
                .orElseThrow(() -> new BizException("版本不存在"));

        //已持久化 0:否 1:是
        if (versionPo.getIsPersisted() == 1) {
            log.info("版本已持久化 中继通道:{} 文档路径:{} 版本号:{}", versionPo.getRelayServer().getName(), versionPo.getEndpointDoc().getDocPullUrl(), versionPo.getVersion());
            return;
        }

        //获取端点文档
        EndpointDocPo epDocPo = versionPo.getEndpointDoc();

        //获取中继通道
        RelayServerPo relayServerPo = epDocPo.getRelayServer();


        List<EndpointDocOperationPo> operationPos = extractOperations(openAPI);

        for (EndpointDocOperationPo po : operationPos) {
            po.setRelayServer(relayServerPo);
            po.setEpDoc(epDocPo);
            po.setVersion(versionPo);
        }

        versionPo.setIsPersisted(1);
        endpointDocVersionRepository.save(versionPo);
        endpointDocOperationRepository.saveAll(operationPos);
    }


    /**
     * 提取端点文档接口
     *
     * @param openApi 端点文档
     * @return 端点文档接口列表
     */
    public List<EndpointDocOperationPo> extractOperations(OpenAPI openApi) {

        List<EndpointDocOperationPo> operationPos = new ArrayList<>();

        // 1. 关键改动：遍历entrySet()可以同时获取到路径字符串(Key)和路径项(Value)
        for (Map.Entry<String, PathItem> pathEntry : openApi.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();

            // 2. 核心：一个PathItem包含多个HTTP方法，需要分别处理
            // 这里我们将所有方法和对应的Operation对象放入一个Map中统一处理，更优雅
            Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();

            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operationMap.entrySet()) {
                String httpMethod = operationEntry.getKey().name().toUpperCase(); // GET, POST...
                Operation operation = operationEntry.getValue();

                // 如果operation为空，则跳过（虽然很少见）
                if (operation == null) {
                    continue;
                }

                EndpointDocOperationPo po = new EndpointDocOperationPo();

                // --- 设置基础关联信息 ---
                // po.setRelayServer(relayServerPo);
                // po.setEpDoc(epDocPo);
                // po.setVersion(versionPo);

                // --- 从解析结果中获取并设置字段 ---

                // 设置路径和方法
                po.setPath(path);
                po.setMethod(httpMethod);

                // 设置基本信息 (summary, description, operationId)
                po.setSummary(operation.getSummary());
                po.setDescription(operation.getDescription());
                po.setOperationId(operation.getOperationId());

                // 设置标签 (Tags)，规范中是列表，我们合并成一个字符串
                if (operation.getTags() != null && !operation.getTags().isEmpty()) {
                    po.setTag(String.join(",", operation.getTags()));
                }

                // --- 处理复杂的JSON字段 ---

                // 设置请求参数 (Query, Header, Path, Cookie)
                // 将所有参数都序列化，也可根据 in 字段进行过滤
                try {
                    po.setReqQueryJson(gson.toJson(operation.getParameters()));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                // 设置请求体 (Request Body)
                BodySchema reqSchema = JsonRequestBodySchemaParser.parse(openApi, operation);
                if (reqSchema != null) {
                    po.setReqBodyJson(gson.toJson(reqSchema));
                }

                // 设置响应集合 (Responses)
                // resQueryJson` 字段可能意指存储所有响应，这里我们存入完整的Responses对象
                po.setResQueryJson(gson.toJson(operation.getResponses()));

                BodySchema resSchema = JsonResponseBodySchemaParser.parse(openApi, operation);
                if (resSchema != null) {
                    po.setResBodyJson(gson.toJson(resSchema));
                }

                // 设置创建时间
                po.setCreateTime(LocalDateTime.now());
                operationPos.add(po);
            }
        }
        return operationPos;
    }


}
