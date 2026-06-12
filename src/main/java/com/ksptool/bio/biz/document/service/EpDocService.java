package com.ksptool.bio.biz.document.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.document.commons.OpenApiUtils;
import com.ksptool.bio.biz.document.model.epdoc.*;
import com.ksptool.bio.biz.document.model.epdocsynclog.EndpointDocSyncLogPo;
import com.ksptool.bio.biz.document.model.epdocversion.EndpointDocVersionPo;
import com.ksptool.bio.biz.document.repository.EndpointDocRepository;
import com.ksptool.bio.biz.document.repository.EndpointDocSyncLogRepository;
import com.ksptool.bio.biz.document.repository.EndpointDocVersionRepository;
import com.ksptool.bio.biz.relay.model.relayserver.RelayServerPo;
import com.ksptool.bio.biz.relay.repository.RelayServerRepository;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EpDocService {

    @Autowired
    private EndpointDocRepository relayDocPullConfigRepository;

    @Autowired
    private EndpointDocSyncLogRepository endpointDocSyncLogRepository;

    @Autowired
    private RelayServerRepository relayServerRepository;

    @Autowired
    private EndpointDocVersionRepository endpointDocVersionRepository;

    @Autowired
    private EpDocOperationService epDocOperationService;

    @Transactional(rollbackFor = Exception.class)
    public void pullEpDoc(Long epDocId) throws BizException, JsonProcessingException {

        EndpointDocPo epDoc = relayDocPullConfigRepository.findById(epDocId)
                .orElseThrow(() -> new BizException("文档拉取配置不存在"));

        String pullUrl = epDoc.getDocPullUrl();

        //创建拉取记录
        EndpointDocSyncLogPo syncLogPo = new EndpointDocSyncLogPo();
        syncLogPo.setEndpointDoc(epDoc);
        syncLogPo.setHash(null);
        syncLogPo.setPullUrl(pullUrl);
        syncLogPo.setStatus(1); //状态 0: 成功 1: 失败
        syncLogPo.setNewVersionCreated(0); //是否创建了新版本 0: 否 1: 是
        syncLogPo.setNewVersionNum(null); //新版本号，如果创建了新版本，则记录新版本号
        syncLogPo.setCreateTime(LocalDateTime.now());

        try {

            //ParseOptions options = new ParseOptions();
            //options.setResolve(true);
            ////options.setResolveFully(true);
            ////options.setResolveCombinators(true);
            ////options.setResolveRequestBody(true);
            //options.setResolveResponses(true);

            //从该地址拉取Swagger文档
            //OpenAPI openAPI = new OpenAPIV3Parser().read(pullUrl, null, options);
            OpenAPI openAPI = null;

            //判断文档是否完整
            if (!OpenApiUtils.isComplete(openAPI)) {
                log.error("拉取文档失败，文档不完整 :{}", pullUrl);
                syncLogPo.setStatus(1); //0: 成功 1: 失败
                return;
            }

            //获取文档HASH
            String hash = OpenApiUtils.getHash(openAPI);
            syncLogPo.setHash(hash);
            syncLogPo.setStatus(0); //0: 成功 1: 失败

            //根据Hash查询文档版本
            EndpointDocVersionPo versionPo = endpointDocVersionRepository.getLatestByHash(hash);

            //版本存在说明文档未变更
            if (versionPo != null) {
                syncLogPo.setNewVersionCreated(0); //是否创建了新版本 0: 否 1: 是
                syncLogPo.setNewVersionNum(versionPo.getVersion()); //新版本号，如果创建了新版本，则记录新版本号
                epDocOperationService.saveOperation(openAPI, versionPo.getId());
                log.info("文档未变更 中继通道:{} 文档路径:{} 版本号:{}", epDoc.getRelayServer().getName(), epDoc.getDocPullUrl(), versionPo.getVersion());
            }

            //如果版本不存在则表示文档有变更 需获取本地最新版本号
            if (versionPo == null) {

                //获取当前最新版本
                EndpointDocVersionPo latestVersionPo = endpointDocVersionRepository.getLatestVersion(epDocId);
                Long latestVersionNum = 1L;

                if (latestVersionPo != null) {
                    latestVersionNum = latestVersionPo.getVersion() + 1;

                    //将当前最新版本设置为非最新版本
                    latestVersionPo.setIsLatest(0);
                    endpointDocVersionRepository.save(latestVersionPo);
                }

                //创建新版本
                EndpointDocVersionPo newVersionPo = new EndpointDocVersionPo();
                newVersionPo.setRelayServer(epDoc.getRelayServer());
                newVersionPo.setEndpointDoc(epDoc);
                newVersionPo.setVersion(latestVersionNum);
                newVersionPo.setHash(hash);
                newVersionPo.setIsLatest(1);
                newVersionPo.setApiTotal(openAPI.getPaths().size());
                newVersionPo.setDocJson(OpenApiUtils.toJson(openAPI));
                newVersionPo.setIsPersisted(0); //是否已持久化 0:否 1:是
                newVersionPo.setCreateTime(LocalDateTime.now());
                endpointDocVersionRepository.save(newVersionPo);
                syncLogPo.setNewVersionCreated(1); //是否创建了新版本 0: 否 1: 是
                syncLogPo.setNewVersionNum(latestVersionNum); //新版本号，如果创建了新版本，则记录新版本号
                log.info("为文档创建新版本 中继通道:{} 文档路径:{} 新版本号:{}", epDoc.getRelayServer().getName(), epDoc.getDocPullUrl(), latestVersionNum);

                //持久化版本
                epDocOperationService.saveOperation(openAPI, newVersionPo.getId());
            }


            //拉取成功后更新拉取时间
            epDoc.setPullTime(LocalDateTime.now());
            relayDocPullConfigRepository.save(epDoc);

        } catch (Exception e) {
            log.error("拉取文档失败", e);
            syncLogPo.setStatus(1); //0: 成功 1: 失败
        } finally {
            syncLogPo.setCreateTime(LocalDateTime.now());
            endpointDocSyncLogRepository.save(syncLogPo);
        }

    }

    /**
     * 获取文档拉取配置列表
     *
     * @param dto 分页查询参数
     * @return
     */
    public PageResult<GetEpDocListVo> getEpDocList(GetEpDocListDto dto) {

        Page<EndpointDocPo> pPos = relayDocPullConfigRepository.getRelayDocPullConfigList(dto.pageRequest());
        List<GetEpDocListVo> vos = new ArrayList<>();

        for (EndpointDocPo p : pPos) {
            GetEpDocListVo vo = new GetEpDocListVo();
            vo.setId(p.getId());
            vo.setRelayServerId(p.getRelayServer().getId());
            vo.setRelayServerName(p.getRelayServer().getName());
            vo.setDocPullUrl(p.getDocPullUrl());
            vo.setPullTime(p.getPullTime());
            vo.setCreateTime(p.getCreateTime());
            vos.add(vo);
        }

        return PageResult.success(vos, pPos.getTotalElements());
    }

    /**
     * 添加文档拉取配置
     *
     * @param dto 添加参数
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void addEpDoc(AddEpDocDto dto) throws BizException {

        // 查询中继通道是否存在
        RelayServerPo relayServer = relayServerRepository.findById(dto.getRelayServerId())
                .orElseThrow(() -> new BizException("中继通道不存在"));

        EndpointDocPo insertPo = new EndpointDocPo();
        insertPo.setRelayServer(relayServer);
        insertPo.setDocPullUrl(dto.getDocPullUrl());
        insertPo.setPullTime(null);
        insertPo.setCreateTime(LocalDateTime.now());
        relayDocPullConfigRepository.save(insertPo);
    }

    /**
     * 查询详情
     *
     * @param id 文档拉取配置ID
     * @return
     */
    public GetEpDocDetailsVo getEpDocDetails(Long id) throws BizException {

        EndpointDocPo relayDocPullConfig = relayDocPullConfigRepository.findById(id)
                .orElseThrow(() -> new BizException("文档拉取配置不存在"));

        GetEpDocDetailsVo vo = new GetEpDocDetailsVo();
        vo.setId(relayDocPullConfig.getId());
        vo.setRelayServerId(relayDocPullConfig.getRelayServer().getId());
        vo.setRelayServerName(relayDocPullConfig.getRelayServer().getName());
        vo.setDocPullUrl(relayDocPullConfig.getDocPullUrl());
        vo.setPullTime(relayDocPullConfig.getPullTime());
        vo.setCreateTime(relayDocPullConfig.getCreateTime());
        return vo;
    }

    /**
     * 编辑文档拉取配置
     *
     * @param dto 编辑参数
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void editEpDoc(EditEpDocDto dto) throws BizException {

        // 查询文档拉取配置是否存在
        EndpointDocPo relayDocPullConfig = relayDocPullConfigRepository.findById(dto.getId())
                .orElseThrow(() -> new BizException("文档拉取配置不存在"));

        // 查询中继通道是否存在
        RelayServerPo relayServer = relayServerRepository.findById(dto.getRelayServerId())
                .orElseThrow(() -> new BizException("中继通道不存在"));

        relayDocPullConfig.setRelayServer(relayServer);
        relayDocPullConfig.setDocPullUrl(dto.getDocPullUrl());
        relayDocPullConfig.setPullTime(null);
        relayDocPullConfigRepository.save(relayDocPullConfig);
    }


    /**
     * 删除文档拉取配置
     *
     * @param id 文档拉取配置ID
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteEpDoc(Long id) throws BizException {

        // 查询文档拉取配置是否存在
        EndpointDocPo relayDocPullConfig = relayDocPullConfigRepository.findById(id)
                .orElseThrow(() -> new BizException("文档拉取配置不存在"));

        relayDocPullConfigRepository.delete(relayDocPullConfig);
    }


}
