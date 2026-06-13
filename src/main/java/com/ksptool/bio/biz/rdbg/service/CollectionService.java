package com.ksptool.bio.biz.rdbg.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.bio.biz.drive.service.EntryAccessService;
import com.ksptool.bio.biz.rdbg.commons.model.*;
import com.ksptool.bio.biz.rdbg.model.collection.CollectionPo;
import com.ksptool.bio.biz.rdbg.model.collection.dto.AddCollectionDto;
import com.ksptool.bio.biz.rdbg.model.collection.dto.EditCollectionDto;
import com.ksptool.bio.biz.rdbg.model.collection.dto.MoveCollectionDto;
import com.ksptool.bio.biz.rdbg.model.collection.vo.GetCollectionDetailsVo;
import com.ksptool.bio.biz.rdbg.model.collection.vo.GetCollectionTreeVo;
import com.ksptool.bio.biz.rdbg.model.collectionhistory.CollectionHistoryPo;
import com.ksptool.bio.biz.rdbg.model.collectionhistory.vo.GetCollectionHistoryDetailsVo;
import com.ksptool.bio.biz.rdbg.repository.CollectionHistoryRepository;
import com.ksptool.bio.biz.rdbg.repository.CollectionRepository;
import com.ksptool.bio.commons.utils.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.ksptool.bio.biz.auth.service.SessionService.session;
import static com.ksptool.entities.Entities.*;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository repository;

    @Autowired
    private CollectionHistoryRepository collectionHistoryRepository;

    @Autowired
    private EntryAccessService entryAccessService;


    private HttpRelay httpRelay;

    /**
     * 获取请求集合树
     *
     * @return 请求集合树
     */
    public List<GetCollectionTreeVo> getCollectionTree() throws Exception {

        //获取当前公司ID
        Long companyId = session().getRootId();

        //获取公司所拥有的全部请求集合树节点
        List<CollectionPo> nodePos = repository.getCollectionTreeListByCompanyId(companyId);

        List<GetCollectionTreeVo> flatNodeVos = new ArrayList<>();
        List<GetCollectionTreeVo> treeNodeVos = new ArrayList<>();

        //处理PO为平面节点
        for (CollectionPo item : nodePos) {
            GetCollectionTreeVo vo = as(item, GetCollectionTreeVo.class);
            if (item.getParent() != null) {
                vo.setParentId(item.getParent().getId());
            }
            vo.setChildren(new ArrayList<>());
            flatNodeVos.add(vo);
        }

        //将平面节点转换为树结构
        Map<Long, GetCollectionTreeVo> idToNode = new HashMap<>();

        //先将所有节点添加到映射中
        for (GetCollectionTreeVo node : flatNodeVos) {
            idToNode.put(node.getId(), node);
        }

        //构建树结构
        for (GetCollectionTreeVo node : flatNodeVos) {
            Long parentId = node.getParentId();
            if (parentId == null) {
                treeNodeVos.add(node);
                continue;
            }
            GetCollectionTreeVo parent = idToNode.get(parentId);
            if (parent == null) {
                treeNodeVos.add(node);
                continue;
            }
            parent.getChildren().add(node);
        }

        return treeNodeVos;
    }

    /**
     * 新增请求集合
     *
     * @param dto 新增请求集合参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void addCollection(AddCollectionDto dto) throws BizException, Exception {

        Long companyId = session().getRootId();
        CollectionPo parentPo = null;

        //如果父级ID不为空，则校验父级节点
        if (dto.getParentId() != null) {

            parentPo = repository.getByIdAndCompanyId(dto.getParentId(), companyId);
            if (parentPo == null) {
                throw new BizException("父级节点不存在或无权限访问");
            }

            if (parentPo.getKind() != 1) {
                throw new BizException("父级节点必须是请求组");
            }

        }

        //组装请求PO
        CollectionPo insertPo = new CollectionPo();
        as(dto, insertPo);
        insertPo.setParent(parentPo);
        insertPo.setCompanyId(companyId);
        insertPo.setSeq(repository.getMaxSeqInParent(dto.getParentId()));

        //如果是请求类型 加入默认请求头、默认请求体
        if (dto.getKind() == 0) {
            insertPo.setReqHeaderJson(toJson(RelayHeader.ofDefaultList()));
            insertPo.setReqBodyJson(toJson(RelayBody.ofDefault()));
        }

        repository.save(insertPo);
    }

    /**
     * 移动请求集合
     *
     * @param dto 移动请求集合参数
     * @return 移动请求集合结果
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveCollection(MoveCollectionDto dto) throws BizException, Exception {

        Long companyId = session().getRootId();

        //查找对象节点
        CollectionPo nodePo = repository.getByIdAndCompanyId(dto.getNodeId(), companyId);

        if (nodePo == null) {
            throw new BizException("对象节点不存在或无权限访问");
        }

        //移动到顶层
        if (dto.getTargetId() == null) {
            nodePo.setParent(null);
            nodePo.setSeq(repository.getMaxSeqInParent(null));
            repository.save(nodePo);
            return;
        }

        //移动到目标节点
        CollectionPo targetNodePo = repository.getByIdAndCompanyId(dto.getTargetId(), companyId);

        if (targetNodePo == null) {
            throw new BizException("目标节点不存在或无权限访问");
        }

        Integer targetSeq = targetNodePo.getSeq();

        //处理移动方式 0:顶部 1:底部 2:内部

        //顶部
        if (dto.getKind() == 0) {

            nodePo.setSeq(targetSeq);

            /*
             * 处理目标节点让位
             * 让位逻辑: 目标节点同级节点下所有节点SEQ+1
             */

            //获取目标节点的父级 如果是NULL则顶级节点让位
            CollectionPo targetParentNodePo = targetNodePo.getParent();

            //将当前正在移动的节点父级设置为目标节点的父级
            nodePo.setParent(targetParentNodePo);

            //处理顶级节点让位
            if (targetParentNodePo == null) {
                List<CollectionPo> rootNodeList = repository.getRootNodeListByCompanyId(companyId);
                for (CollectionPo item : rootNodeList) {
                    if (item.getSeq() >= targetSeq) {
                        item.setSeq(item.getSeq() + 1);
                    }
                }
                repository.saveAll(rootNodeList);
                repository.save(nodePo);
                return;
            }

            //处理内部节点让位
            List<CollectionPo> targetParentNodeList = targetParentNodePo.getChildren();
            for (CollectionPo item : targetParentNodeList) {
                if (item.getSeq() >= targetSeq) {

                    //跳过当前正在移动的节点
                    if (Objects.equals(item.getId(), nodePo.getId())) {
                        continue;
                    }

                    item.setSeq(item.getSeq() + 1);
                }
            }
            repository.saveAll(targetParentNodeList);
            repository.save(nodePo);
            return;
        }

        //底部
        if (dto.getKind() == 1) {
            nodePo.setSeq(targetSeq);

            /*
             * 处理目标节点让位
             * 让位逻辑: 目标节点同级节点下所有节点SEQ-1
             */
            //获取目标节点的父级 如果是NULL则顶级节点让位
            CollectionPo targetParentNodePo = targetNodePo.getParent();

            //将当前正在移动的节点父级设置为目标节点的父级
            nodePo.setParent(targetParentNodePo);

            //处理顶级节点让位
            if (targetParentNodePo == null) {
                List<CollectionPo> rootNodeList = repository.getRootNodeListByCompanyId(companyId);
                for (CollectionPo item : rootNodeList) {
                    if (item.getSeq() <= targetSeq) {

                        //跳过当前正在移动的节点
                        if (Objects.equals(item.getId(), nodePo.getId())) {
                            continue;
                        }


                        item.setSeq(item.getSeq() - 1);
                    }
                }
                repository.saveAll(rootNodeList);
                repository.save(nodePo);
                return;
            }

            //处理内部节点让位
            List<CollectionPo> targetParentNodeList = targetParentNodePo.getChildren();
            for (CollectionPo item : targetParentNodeList) {
                if (item.getSeq() <= targetSeq) {

                    //跳过当前正在移动的节点
                    if (Objects.equals(item.getId(), nodePo.getId())) {
                        continue;
                    }

                    item.setSeq(item.getSeq() - 1);
                }
            }
            repository.saveAll(targetParentNodeList);
            repository.save(nodePo);
            return;
        }

        //内部
        if (dto.getKind() == 2) {

            //目标节点不是组则直接移动到下一个位置 并处理让位
            if (targetNodePo.getKind() == 0) {
                nodePo.setSeq(targetSeq);

                //获取目标节点的父级 如果是NULL则顶级节点让位
                CollectionPo targetParentNodePo = targetNodePo.getParent();

                //将当前正在移动的节点父级设置为目标节点的父级
                nodePo.setParent(targetParentNodePo);

                //处理顶级节点让位
                if (targetParentNodePo == null) {
                    List<CollectionPo> rootNodeList = repository.getRootNodeListByCompanyId(companyId);
                    for (CollectionPo item : rootNodeList) {

                        //跳过当前正在移动的节点
                        if (Objects.equals(item.getId(), nodePo.getId())) {
                            continue;
                        }

                        if (item.getSeq() >= targetSeq) {
                            item.setSeq(item.getSeq() + 1);
                        }
                    }
                    repository.saveAll(rootNodeList);
                    repository.save(nodePo);
                    return;
                }

                //处理内部节点让位
                List<CollectionPo> targetParentNodeList = targetParentNodePo.getChildren();
                for (CollectionPo item : targetParentNodeList) {
                    if (item.getSeq() >= targetSeq) {

                        //跳过当前正在移动的节点
                        if (Objects.equals(item.getId(), nodePo.getId())) {
                            continue;
                        }

                        item.setSeq(item.getSeq() + 1);
                    }
                }
                repository.saveAll(targetParentNodeList);
                repository.save(nodePo);
                return;
            }

            //目标节点是组则移动到组中最后一个位置
            nodePo.setParent(targetNodePo);
            nodePo.setSeq(repository.getMaxSeqInParent(targetNodePo.getId()));
            repository.save(nodePo);
            return;
        }

    }


    /**
     * 复制请求集合
     *
     * @param dto 复制请求集合参数
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void copyCollection(CommonIdDto dto) throws BizException, Exception {

        Long companyId = session().getRootId();

        CollectionPo sourceNodePo = repository.getByIdAndCompanyId(dto.getId(), companyId);

        if (sourceNodePo == null) {
            throw new BizException("对象节点不存在或无权限访问");
        }

        /*
         * 处理节点基础信息
         * 新复制的节点排序为当前节点排序+1且父级为当前节点的父级
         * 新复制的节点将插入在被复制节点之后的一个槽位,并且后方所有大于等于 新复制节点SEQ 的节点都需要进行让位处理
         * 让位规则: 节点SEQ >= 新复制节点SEQ 则节点SEQ + 1
         */
        var nodeName = sourceNodePo.getName() + " 副本";

        //组装新节点PO
        CollectionPo newNodePo = new CollectionPo();
        as(sourceNodePo, newNodePo);
        newNodePo.setId(IdWorker.nextId());
        newNodePo.setName(nodeName);
        newNodePo.setSeq(sourceNodePo.getSeq() + 1);

        //处理让位
        var sourceParentNodePo = sourceNodePo.getParent();

        //处理顶级节点让位
        if (sourceParentNodePo == null) {

            List<CollectionPo> rootNodeList = repository.getRootNodeListByCompanyId(companyId);
            var startSeq = newNodePo.getSeq() + 1;

            for (CollectionPo item : rootNodeList) {

                //所有SEQ大于等于新复制节点SEQ的节点都需要进行让位处理
                if (item.getSeq() >= newNodePo.getSeq()) {
                    item.setSeq(startSeq++);
                }

            }
            repository.saveAll(rootNodeList);
        }

        //处理内部节点让位
        if (sourceParentNodePo != null) {

            List<CollectionPo> sourceParentNodeList = sourceParentNodePo.getChildren();
            for (CollectionPo item : sourceParentNodeList) {
                if (item.getSeq() >= newNodePo.getSeq()) {
                    item.setSeq(item.getSeq() + 1);
                }
            }
            repository.saveAll(sourceParentNodeList);

        }


        //如果被复制的节点是组则复制组下的所有请求
        if (sourceNodePo.getKind() == 1) {
            var sourceChildren = sourceNodePo.getChildren();
            List<CollectionPo> targetChildren = new ArrayList<>();
            for (CollectionPo item : sourceChildren) {
                CollectionPo newChild = new CollectionPo();
                as(item, newChild);
                newChild.setId(null);
                newChild.setParent(newNodePo);
                targetChildren.add(newChild);
            }
            newNodePo.setChildren(targetChildren);
        }

        repository.save(newNodePo);
    }


    /**
     * 编辑请求集合
     *
     * @param dto 编辑请求集合参数
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editCollection(EditCollectionDto dto) throws BizException, Exception {

        Long companyId = session().getRootId();
        CollectionPo updatePo = repository.getByIdAndCompanyId(dto.getId(), companyId);

        if (updatePo == null) {
            throw new BizException("对象节点不存在或无权限访问");
        }

        //如果是组类型不允许编辑请求相关字段
        if (updatePo.getKind() == 1) {
            if (StringUtils.isNotBlank(dto.getReqUrl())) {
                throw new BizException("组类型不允许编辑请求URL");
            }
            if (dto.getRequestParams() != null && !dto.getRequestParams().isEmpty()) {
                throw new BizException("组类型不允许编辑请求URL参数");
            }
            if (dto.getReqMethod() != null) {
                throw new BizException("组类型不允许编辑请求方法");
            }
            if (dto.getRequestHeaders() != null && !dto.getRequestHeaders().isEmpty()) {
                throw new BizException("组类型不允许编辑请求头");
            }
            if (dto.getReqBodyKind() != null) {
                throw new BizException("组类型不允许编辑请求体类型");
            }
            if (dto.getReqBody() != null) {
                throw new BizException("组类型不允许编辑请求体");
            }
        }

        updatePo.setName(dto.getName());
        updatePo.setReqUrl(dto.getReqUrl());
        updatePo.setReqUrlParamsJson(toJson(dto.getRequestParams()));
        updatePo.setReqMethod(dto.getReqMethod());
        updatePo.setReqHeaderJson(toJson(dto.getRequestHeaders()));
        updatePo.setReqBodyKind(dto.getReqBodyKind());
        updatePo.setReqBodyJson(toJson(dto.getReqBody()));
        repository.save(updatePo);
    }

    /**
     * 获取请求集合详情
     *
     * @param dto 获取请求集合详情参数
     * @return 请求集合详情
     * @throws BizException 业务异常
     */
    public GetCollectionDetailsVo getCollectionDetails(CommonIdDto dto) throws BizException, Exception {

        Long companyId = session().getRootId();
        CollectionPo po = repository.getByIdAndCompanyId(dto.getId(), companyId);
        if (po == null) {
            throw new BizException("对象节点不存在或无权限访问");
        }

        GetCollectionDetailsVo vo = as(po, GetCollectionDetailsVo.class);

        if (po.getParent() != null) {
            vo.setParentId(po.getParent().getId());
        }

        //解析请求URL参数、请求头、请求体
        if (po.getKind() == 0) {
            vo.setRequestParams(fromJsonArray(po.getReqUrlParamsJson(), RelayParam.class));
            vo.setRequestHeaders(fromJsonArray(po.getReqHeaderJson(), RelayHeader.class));
            vo.setReqBody(fromJson(po.getReqBodyJson(), RelayBody.class));

            if (StringUtils.isBlank(po.getReqBodyJson())) {
                vo.setReqBody(RelayBody.ofDefault());
            }

        }

        return vo;
    }

    /**
     * 删除请求集合
     *
     * @param dto 删除请求集合参数
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeCollection(CommonIdDto dto) throws BizException, Exception {

        Long companyId = session().getRootId();

        var ids = dto.toIds();

        for (var id : ids) {
            CollectionPo po = repository.getByIdAndCompanyId(id, companyId);
            if (po == null) {
                throw new BizException("对象节点不存在或无权限访问");
            }
            repository.delete(po);
        }

    }

    /**
     * 发送请求
     *
     * @param dto 发送请求参数
     * @return 发送请求结果
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public GetCollectionHistoryDetailsVo sendRequest(CommonIdDto dto) throws BizException, Exception {

        var companyId = session().getRootId();

        CollectionPo po = repository.getByIdAndCompanyId(dto.getId(), companyId);

        if (po == null) {
            throw new BizException("对象节点不存在或无权限访问");
        }

        if (po.getKind() != 0) {
            throw new BizException("对象节点必须是请求类型");
        }

        //读取Collection信息
        var reqUrl = po.getReqUrl();
        var reqUrlParams = fromJsonArray(po.getReqUrlParamsJson(), RelayParam.class);
        var reqMethod = po.getReqMethod();
        var reqHeaders = fromJsonArray(po.getReqHeaderJson(), RelayHeader.class);
        var reqBody = fromJson(po.getReqBodyJson(), RelayBody.class);

        //组装relay引擎schema
        var schema = HttpRelaySchema.of(reqUrl, reqMethod);
        schema.setQueryParams(reqUrlParams);
        schema.setHeaders(new HashSet<>(reqHeaders));
        schema.setBody(reqBody);
        schema.setConfig(HttpRelaySchemaConfig.ofDefault());

        //记录默认响应
        var historyPo = new CollectionHistoryPo();
        historyPo.setId(IdWorker.nextId());
        historyPo.setCompanyId(companyId);
        historyPo.setCollectionId(po.getId());
        historyPo.setReqUrl(reqUrl);
        historyPo.setReqUrlParamsJson(toJson(reqUrlParams));
        historyPo.setReqMethod(reqMethod);
        historyPo.setReqHeaderJson(toJson(reqHeaders));
        historyPo.setReqBodyJson(toJson(reqBody));
        historyPo.setRetHeaderJson("[]");
        historyPo.setRetBodyText("");
        historyPo.setRetHttpStatus(null);
        historyPo.setBizStatus(3); //0:正常 1:HTTP失败 2:业务失败 3:正在处理 4:EAS内部错误
        historyPo.setErrorMessage(null);
        historyPo.setReqTime(LocalDateTime.now());
        historyPo.setRetTime(null);

        HttpRelayResponse hrr = null;

        try {

            hrr = httpRelay.sendRequest(schema);

            //读取响应内容

        } catch (Exception e) {
            historyPo.setBizStatus(4);
            historyPo.setErrorMessage(e.getMessage());
            collectionHistoryRepository.save(historyPo);

            //构建失败结果
            var retVo = new GetCollectionHistoryDetailsVo();
            as(historyPo, retVo);
            retVo.setBizStatus(4);
            retVo.setErrorMessage(e.getMessage());
            return retVo;
        }

        //记录成功响应
        historyPo.setRetHeaderJson(toJson(hrr.getHeaders()));
        historyPo.setRetHttpStatus(hrr.getResponse().statusCode());
        historyPo.setBizStatus(0);
        historyPo.setErrorMessage(null);
        historyPo.setRetTime(LocalDateTime.now());
        collectionHistoryRepository.save(historyPo);


        //构建成功结果
        var retVo = new CollectionService();

        return null;
    }

}