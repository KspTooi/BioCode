package com.ksptool.bio.biz.assembly.service;

import com.google.gson.Gson;
import com.ksptool.assembly.entity.exception.AuthException;
import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.assembly.common.quickbuildengine.QbeBlueprint;
import com.ksptool.bio.biz.assembly.common.quickbuildengine.QbeBlueprintReader;
import com.ksptool.bio.biz.assembly.common.quickbuildengine.QbeModel;
import com.ksptool.bio.biz.assembly.common.quickbuildengine.QbeVelocityEngine;
import com.ksptool.bio.biz.assembly.model.opschema.OpSchemaPo;
import com.ksptool.bio.biz.assembly.model.opschema.dto.AddOpSchemaDto;
import com.ksptool.bio.biz.assembly.model.opschema.dto.CopyOpSchemaDto;
import com.ksptool.bio.biz.assembly.model.opschema.dto.EditOpSchemaDto;
import com.ksptool.bio.biz.assembly.model.opschema.dto.ExecuteOpSchemaDto;
import com.ksptool.bio.biz.assembly.model.opschema.dto.GetOpSchemaListDto;
import com.ksptool.bio.biz.assembly.model.opschema.vo.GetOpBluePrintListVo;
import com.ksptool.bio.biz.assembly.model.opschema.vo.GetOpSchemaDetailsVo;
import com.ksptool.bio.biz.assembly.model.opschema.vo.GetOpSchemaListVo;
import com.ksptool.bio.biz.assembly.model.oprcd.OpRcdPo;
import com.ksptool.bio.biz.assembly.model.polymodel.PolyModelPo;
import com.ksptool.bio.biz.assembly.model.rawmodel.RawModelPo;
import com.ksptool.bio.biz.assembly.model.scm.ScmPo;
import com.ksptool.bio.biz.assembly.model.tymschema.TymSchemaPo;
import com.ksptool.bio.biz.assembly.model.tymschemafield.TymSchemaFieldPo;
import com.ksptool.bio.biz.assembly.repository.*;
import com.ksptool.bio.biz.core.service.AttachService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ksptool.bio.biz.auth.service.SessionService.session;
import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Slf4j
@Service
public class OpSchemaService {

    //QBE Velocity引擎实例
    private final QbeVelocityEngine qbeVelocityEngine = new QbeVelocityEngine();
    @Autowired
    private Gson gson;
    @Autowired
    private OpSchemaRepository repository;
    @Autowired
    private DataSourceRepository datasourceRepository;
    @Autowired
    private RawModelService rawModelService;
    @Autowired
    private RawModelRepository rawModelRepository;
    @Autowired
    private ScmRepository scmRepository;
    @Autowired
    private AttachService attachService;
    @Autowired
    private ScmService scmService;
    @Autowired
    private PolyModelService polyModelService;
    @Autowired
    private PolyModelRepository polyModelRepository;
    @Autowired
    private TymSchemaRepository tymSchemaRepository;
    @Autowired
    private TymSchemaFieldRepository tymSfRepository;
    @Autowired
    private OpRcdRepository opRcdRepository;

    /**
     * 查询输出方案列表
     *
     * @param dto 查询参数
     * @return 输出方案列表
     */
    public PageResult<GetOpSchemaListVo> getOpSchemaList(GetOpSchemaListDto dto) {
        OpSchemaPo query = new OpSchemaPo();
        assign(dto, query);

        Page<OpSchemaPo> page = repository.getOpSchemaList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetOpSchemaListVo> vos = as(page.getContent(), GetOpSchemaListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增输出方案
     *
     * @param dto 新增参数
     */
    @Transactional(rollbackFor = Exception.class)
    public String addOpSchema(AddOpSchemaDto dto) throws Exception {

        if (repository.countByNameExcludeId(dto.getName(), null) > 0) {
            throw new BizException("新增失败,输出方案名称已存在.");
        }

        OpSchemaPo insertPo = as(dto, OpSchemaPo.class);

        var session = session();
        var userId = session.getUserId();

        if (dto.getInputScmId() != null) {

            ScmPo inputScmPo = scmRepository.findById(dto.getInputScmId())
                    .orElseThrow(() -> new BizException("新增输出方案失败,输入SCM不存在或无权限访问."));

            if (!Objects.equals(inputScmPo.getCreatorId(), userId)) {
                throw new BizException("新增输出方案失败,无权访问SCM。");
            }

        }

        if (dto.getOutputScmId() != null) {
            ScmPo outputScmPo = scmRepository.findById(dto.getOutputScmId())
                    .orElseThrow(() -> new BizException("新增输出方案失败,输出SCM不存在或无权限访问."));

            if (!Objects.equals(outputScmPo.getCreatorId(), userId)) {
                throw new BizException("新增输出方案失败,无权访问SCM。");
            }
        }


        insertPo.setFieldCountOrigin(0);
        insertPo.setFieldCountPoly(0);

        //先保存输出方案，这样才能获取到主键ID
        insertPo = repository.save(insertPo);

        //如果输入数据源和表名,则查询数据源表字段
        if (dto.getDataSourceId() != null && StringUtils.isNotBlank(dto.getTableName())) {

            var dataSource = datasourceRepository.findById(dto.getDataSourceId()).orElse(null);

            if (dataSource == null) {
                return "新增成功,但未能从数据源导入原始字段,数据源不存在或无权限访问！";
            }

            var fields = rawModelService.getDataSourceTableFields(dataSource, dto.getTableName());

            if (fields == null) {
                return "新增成功,但未能从数据源导入原始字段,查询数据源表字段失败！";
            }

            var insertFields = new ArrayList<RawModelPo>();

            int seq = 1;
            for (var field : fields) {
                field.setOutputSchemaId(insertPo.getId());
                field.setSeq(seq++);
                field.setPk(0);
                insertFields.add(field);
            }

            //批量保存原始字段
            rawModelRepository.saveAll(insertFields);

            //更新输出方案字段数量
            insertPo.setFieldCountOrigin(insertFields.size());
            repository.save(insertPo);

            return "新增成功,已从数据源导入" + insertFields.size() + "个原始字段！";
        }

        return "新增成功,未导入任何原始字段！";
    }

    /**
     * 编辑输出方案
     *
     * @param dto 编辑参数
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public String editOpSchema(EditOpSchemaDto dto) throws Exception {

        if (repository.countByNameExcludeId(dto.getName(), dto.getId()) > 0) {
            throw new BizException("修改失败,输出方案名称已存在.");
        }

        OpSchemaPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);

        var session = session();
        var userId = session.getUserId();

        if (dto.getInputScmId() != null) {
            ScmPo inputScmPo = scmRepository.findById(dto.getInputScmId())
                    .orElseThrow(() -> new BizException("新增输出方案失败,输入SCM不存在或无权限访问."));

            if (!Objects.equals(inputScmPo.getCreatorId(), userId)) {
                throw new BizException("修改输出方案失败,无权访问SCM。");
            }
        }

        if (dto.getOutputScmId() != null) {
            ScmPo outputScmPo = scmRepository.findById(dto.getOutputScmId())
                    .orElseThrow(() -> new BizException("新增输出方案失败,输出SCM不存在或无权限访问."));

            if (!Objects.equals(outputScmPo.getCreatorId(), userId)) {
                throw new BizException("修改输出方案失败,无权访问SCM。");
            }
        }

        // 没有配置数据源或表名，直接保存
        if (dto.getDataSourceId() == null || StringUtils.isBlank(dto.getTableName())) {
            repository.save(updatePo);
            return "修改成功";
        }

        var dataSource = datasourceRepository.findById(dto.getDataSourceId()).orElse(null);
        if (dataSource == null) {
            return "未能同步原始字段,数据源不存在或无权限访问！";
        }

        var latestFields = rawModelService.getDataSourceTableFields(dataSource, dto.getTableName());
        if (latestFields == null) {
            return "未能同步原始字段,查询数据源表字段失败！";
        }

        // 已存在的原始字段，以字段名为 key
        List<RawModelPo> existingFields = rawModelRepository.getRawModelByOutputSchemaId(dto.getId());
        Map<String, RawModelPo> existingMap = existingFields.stream()
                .collect(Collectors.toMap(RawModelPo::getName, f -> f));

        // 最新字段名集合，用于检测数据库中已删除的字段
        java.util.Set<String> latestNames = latestFields.stream()
                .map(RawModelPo::getName)
                .collect(Collectors.toSet());

        // 删除数据库中已不存在的字段
        List<RawModelPo> toDelete = existingFields.stream()
                .filter(f -> !latestNames.contains(f.getName()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            rawModelRepository.deleteAll(toDelete);
        }

        // 新增数据库中不存在的字段，已存在的跳过（保留用户可能已修改的数据）
        List<RawModelPo> toInsert = new ArrayList<>();
        int maxSeq = existingFields.stream().mapToInt(RawModelPo::getSeq).max().orElse(0);
        for (var field : latestFields) {
            if (existingMap.containsKey(field.getName())) {
                continue;
            }
            field.setOutputSchemaId(dto.getId());
            field.setSeq(++maxSeq);
            toInsert.add(field);
        }
        if (!toInsert.isEmpty()) {
            rawModelRepository.saveAll(toInsert);
        }

        // 更新字段数量
        int newCount = existingFields.size() - toDelete.size() + toInsert.size();
        updatePo.setFieldCountOrigin(newCount);
        repository.save(updatePo);

        //重映射现有的聚合模型(如果有配置类型映射方案)
        if (dto.getTypeSchemaId() != null) {

            //获取类型映射方案
            TymSchemaPo tymSchemaPo = tymSchemaRepository.findById(dto.getTypeSchemaId())
                    .orElseThrow(() -> new BizException("修改输出方案失败,类型映射方案不存在或无权限访问."));

            //获取类型映射方案下全部的映射类型
            List<TymSchemaFieldPo> tymSfPos = tymSfRepository.getTymSfByTymSid(dto.getTypeSchemaId());

            //先获取现有聚合模型
            List<PolyModelPo> polyModels = polyModelRepository.getPolyModelByOutputSchemaId(dto.getId());

            //获取全部原始模型
            List<RawModelPo> rawModels = rawModelRepository.getRawModelByOutputSchemaId(dto.getId());

            //遍历聚合模型
            for (PolyModelPo polyModel : polyModels) {
                //获取原始模型
                RawModelPo rawModel = rawModels.stream()
                        .filter(r -> r.getName().equals(polyModel.getName()))
                        .findFirst()
                        .orElse(null);

                if (rawModel == null) {
                    continue;
                }

                //更新聚合模型的数据类型，先查找原始模型匹配的映射方案

                TymSchemaFieldPo mat = null;
                var targetType = tymSchemaPo.getDefaultType();

                for (var tymSfPo : tymSfPos) {
                    if (tymSfPo.getSource().equals(rawModel.getDataType())) {
                        mat = tymSfPo;
                        break;
                    }
                }

                //如果匹配到tymSf 则使用tymSf的target类型
                if (mat != null) {
                    targetType = mat.getTarget();
                }

                //更新聚合模型的数据类型
                polyModel.setDataType(targetType);
                polyModelRepository.save(polyModel);
            }

        }

        return "修改成功,新增" + toInsert.size() + "个原始字段,删除" + toDelete.size() + "个原始字段！";
    }

    /**
     * 查询输出方案详情
     *
     * @param dto 查询参数
     * @return 输出方案详情
     * @throws BizException 业务异常
     */
    public GetOpSchemaDetailsVo getOpSchemaDetails(CommonIdDto dto) throws BizException {
        OpSchemaPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetOpSchemaDetailsVo.class);
    }

    /**
     * 删除输出方案元素
     *
     * @param dto 删除参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeOpSchema(CommonIdDto dto) {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }

    /**
     * 复制输出方案(含原始模型和聚合模型)
     *
     * @param dto 源输出方案ID
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void copyOpSchema(CopyOpSchemaDto dto) throws Exception {
        OpSchemaPo source = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("复制失败,数据不存在或无权限访问."));

        if (repository.countByNameExcludeId(dto.getName(), null) > 0) {
            throw new BizException("复制失败,输出方案名称已存在.");
        }

        var userId = session().getUserId();

        //检查两个SCM的可用性
        Long inputScmId = null;
        if (source.getInputScmId() != null) {
            ScmPo inputScmPo = scmRepository.findById(source.getInputScmId()).orElse(null);
            if (inputScmPo != null && Objects.equals(inputScmPo.getCreatorId(), userId)) {
                inputScmId = source.getInputScmId();
            }
        }

        Long outputScmId = null;
        if (source.getOutputScmId() != null) {
            ScmPo outputScmPo = scmRepository.findById(source.getOutputScmId()).orElse(null);
            if (outputScmPo != null && Objects.equals(outputScmPo.getCreatorId(), userId)) {
                outputScmId = source.getOutputScmId();
            }
        }

        //复制基础信息
        OpSchemaPo copy = new OpSchemaPo();
        copy.setDataSourceId(source.getDataSourceId());
        copy.setTypeSchemaId(source.getTypeSchemaId());
        copy.setInputScmId(inputScmId);
        copy.setOutputScmId(outputScmId);
        copy.setName(dto.getName());
        copy.setModelName(source.getModelName());
        copy.setModelRemark(source.getModelRemark());
        copy.setBizDomain(source.getBizDomain());
        copy.setTableName(source.getTableName());
        copy.setRemoveTablePrefix(source.getRemoveTablePrefix());
        copy.setPermCodePrefix(source.getPermCodePrefix());
        copy.setPolicyOverride(source.getPolicyOverride());
        copy.setBaseInput(source.getBaseInput());
        copy.setBaseOutput(source.getBaseOutput());
        copy.setRemark(source.getRemark());
        copy.setFieldCountOrigin(source.getFieldCountOrigin());
        copy.setFieldCountPoly(source.getFieldCountPoly());

        //先保存输出方案，这样才能获取到主键ID
        copy = repository.save(copy);

        Long newId = copy.getId();

        //复制原始模型
        List<RawModelPo> rawModels = rawModelRepository.getRawModelByOutputSchemaId(source.getId());
        if (!rawModels.isEmpty()) {
            List<RawModelPo> rawCopies = new ArrayList<>();
            for (RawModelPo raw : rawModels) {
                RawModelPo rawCopy = new RawModelPo();
                rawCopy.setOutputSchemaId(newId);
                rawCopy.setName(raw.getName());
                rawCopy.setDataType(raw.getDataType());
                rawCopy.setLength(raw.getLength());
                rawCopy.setRequire(raw.getRequire());
                rawCopy.setRemark(raw.getRemark());
                rawCopy.setPk(raw.getPk());
                rawCopy.setSeq(raw.getSeq());
                rawCopies.add(rawCopy);
            }
            rawModelRepository.saveAll(rawCopies);
        }

        List<PolyModelPo> polyModels = polyModelRepository.getPolyModelByOutputSchemaId(source.getId());
        if (!polyModels.isEmpty()) {
            List<PolyModelPo> polyCopies = new ArrayList<>();
            for (PolyModelPo poly : polyModels) {
                PolyModelPo polyCopy = new PolyModelPo();
                polyCopy.setOutputSchemaId(newId);
                polyCopy.setName(poly.getName());
                polyCopy.setDataType(poly.getDataType());
                polyCopy.setLength(poly.getLength());
                polyCopy.setRequire(poly.getRequire());
                polyCopy.setPolicyCrudJson(poly.getPolicyCrudJson());
                polyCopy.setPolicyQuery(poly.getPolicyQuery());
                polyCopy.setPolicyView(poly.getPolicyView());
                polyCopy.setPk(poly.getPk());
                polyCopy.setRemark(poly.getRemark());
                polyCopy.setSeq(poly.getSeq());
                polyCopies.add(polyCopy);
            }
            polyModelRepository.saveAll(polyCopies);
        }
    }

    /**
     * 查询蓝图文件列表
     *
     * @param dto 查询参数
     * @return 蓝图文件列表
     */
    public List<GetOpBluePrintListVo> getOpBluePrintList(CommonIdDto dto) throws Exception {

        OpSchemaPo opSchemaPo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询蓝图文件列表失败,数据不存在或无权限访问."));

        //必须先配置输入SCM
        if (opSchemaPo.getInputScmId() == null) {
            throw new BizException("查询蓝图文件列表失败,输入SCM未配置.");
        }

        //查询输入SCM
        ScmPo inputScmPo = scmRepository.findById(opSchemaPo.getInputScmId())
                .orElseThrow(() -> new BizException("查询蓝图文件列表失败,输入SCM不存在或无权限访问."));

        var session = session();
        var userId = session.getUserId();

        if (!Objects.equals(inputScmPo.getCreatorId(), userId)) {
            throw new BizException("查询蓝图文件列表失败,无权访问SCM。");
        }

        //准备工作空间
        var workSpaceName = "gen_workspace_" + inputScmPo.getId();
        var workSpacePath = attachService.getAttachLocalPath(Paths.get(workSpaceName));
        var workSpaceInputPath = workSpacePath.resolve("input");
        var iAppendPath = opSchemaPo.getBaseInput().trim();  //这是在输出方案中配置的蓝图SCM相对路径
        if (iAppendPath.startsWith("/")) {
            iAppendPath = iAppendPath.substring(1);
        }
        var iBpPath = workSpaceInputPath.resolve(iAppendPath);

        //先检出输入SCM
        scmService.pullFromScm(inputScmPo, workSpaceInputPath.toString());

        //准备QBE模型
        var qbeModel = new QbeModel(opSchemaPo.getTableName(), opSchemaPo.getModelName());
        qbeModel.setBizDomain(opSchemaPo.getBizDomain());

        //使用QBE读取蓝图文件列表
        try {
            QbeBlueprintReader reader = new QbeBlueprintReader(iBpPath.toString());
            List<QbeBlueprint> blueprints = reader.readBlueprint();

            //转换为VO
            var ret = new ArrayList<GetOpBluePrintListVo>();
            for (var blueprint : blueprints) {
                var vo = new GetOpBluePrintListVo();
                vo.setFileName(blueprint.getFileName());
                vo.setFilePath(blueprint.getRelativeFilePath());
                vo.setSha256Hex(blueprint.getSha256Hex());
                blueprint.resolvePath(qbeModel);
                vo.setParsedName(blueprint.getOutputFileName());
                vo.setParsedPath(blueprint.getOutputFilePath());
                ret.add(vo);
            }
            return ret;

        } catch (IOException | IllegalArgumentException e) {
            log.error("读取蓝图文件列表失败: {} 路径: {}", e.getMessage(), workSpaceInputPath, e);
            throw new BizException("读取蓝图文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 预览蓝图输出
     *
     * @param sha256Hex 蓝图SHA256
     * @return 蓝图输出
     * @throws BizException 业务异常
     */
    public String previewOpBluePrint(Long opSchemaId, String sha256Hex) throws BizException {

        OpSchemaPo opSchemaPo = repository.findById(opSchemaId)
                .orElseThrow(() -> new BizException("预览蓝图输出失败,输出方案不存在或无权限访问."));

        var iBpPath = getBluePrintPath(opSchemaPo);

        //查询目录是否存在
        if (!Files.exists(iBpPath)) {
            throw new BizException("预览蓝图输出失败,蓝图目录不存在.");
        }

        QbeBlueprint blueprint;

        //QBE读取所有蓝图文件
        try {
            QbeBlueprintReader reader = new QbeBlueprintReader(iBpPath.toString());
            List<QbeBlueprint> blueprints = reader.readBlueprint();

            //查找指定SHA256的蓝图文件
            blueprint = blueprints.stream()
                    .filter(b -> b.getSha256Hex().equals(sha256Hex))
                    .findFirst()
                    .orElseThrow(() -> new BizException("预览蓝图输出失败,蓝图文件不存在."));

        } catch (IOException e) {
            log.error("预览蓝图输出失败: {} 路径: {}", e.getMessage(), iBpPath, e);
            throw new BizException("预览蓝图输出失败: " + e.getMessage());
        }

        //查询QBE模型
        var qbeModel = polyModelService.getQbeModelByOpSchemaId(opSchemaPo.getId());

        //渲染蓝图
        return qbeVelocityEngine.renderAsString(blueprint.getTemplateContent(), qbeModel, opSchemaPo, null);
    }

    /**
     * 预览Qbe模型
     *
     * @param opSchemaId 输出方案ID
     * @throws BizException 业务异常
     */
    public String previewQbeModel(Long opSchemaId) throws BizException {
        OpSchemaPo opSchemaPo = repository.findById(opSchemaId)
                .orElseThrow(() -> new BizException("预览Qbe模型失败,输出方案不存在或无权限访问."));

        //直接根据方案获取QBE模型
        var qbeModel = polyModelService.getQbeModelByOpSchemaId(opSchemaPo.getId());
        var map = new HashMap<String, Object>();
        map.put("model", qbeModel);
        map.put("schema", opSchemaPo);
        map.put("global", new HashMap<String, Object>());
        return gson.toJson(map);
    }

    /**
     * 执行输出方案
     *
     * @param dto 执行参数
     * @throws BizException 业务异常
     * @throws AuthException 
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeOpSchema(ExecuteOpSchemaDto dto) throws BizException, AuthException {

        var startTime = LocalDateTime.now();

        //查询输出方案
        OpSchemaPo opSchemaPo = repository.findById(dto.getOpSchemaId())
                .orElseThrow(() -> new BizException("执行输出方案失败,数据不存在或无权限访问."));

        //查询输入与输出SCM
        ScmPo inputScmPo = scmRepository.findById(opSchemaPo.getInputScmId())
                .orElseThrow(() -> new BizException("执行输出方案失败,输入SCM不存在或无权限访问."));

        ScmPo outputScmPo = scmRepository.findById(opSchemaPo.getOutputScmId())
                .orElseThrow(() -> new BizException("执行输出方案失败,输出SCM不存在或无权限访问."));

        //查询数据源
        var ds = datasourceRepository.findById(opSchemaPo.getDataSourceId())
                .orElseThrow(() -> new BizException("执行输出方案失败,数据源不存在."));

        //准备工作空间
        var workSpaceName = "gen_workspace_" + inputScmPo.getId();
        var workSpacePath = attachService.getAttachLocalPath(Paths.get(workSpaceName));
        var workSpaceInputPath = workSpacePath.resolve("input");
        var workSpaceOutputPath = workSpacePath.resolve("output");

        //先检出两个SCM
        scmService.pullFromScm(inputScmPo, workSpaceInputPath.toString());
        scmService.pullFromScm(outputScmPo, workSpaceOutputPath.toString());

        var iAppendPath = opSchemaPo.getBaseInput().trim();
        var oAppendPath = opSchemaPo.getBaseOutput().trim();

        if (iAppendPath.startsWith("/")) {
            iAppendPath = iAppendPath.substring(1);
        }
        if (oAppendPath.startsWith("/")) {
            oAppendPath = oAppendPath.substring(1);
        }

        var iBpPath = workSpaceInputPath.resolve(iAppendPath);
        var oPrjPath = workSpaceOutputPath.resolve(oAppendPath);

        //查询QBE模型
        var qbeModel = polyModelService.getQbeModelByOpSchemaId(opSchemaPo.getId());

        //读取蓝图文件列表
        List<QbeBlueprint> blueprints;
        try {
            QbeBlueprintReader reader = new QbeBlueprintReader(iBpPath.toString());
            List<QbeBlueprint> allBlueprints = reader.readBlueprint();

            //按sha256Hex过滤
            blueprints = allBlueprints.stream()
                    .filter(b -> dto.getSha256Hexs().contains(b.getSha256Hex()))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("读取蓝图文件列表失败: {} 路径: {}", e.getMessage(), iBpPath, e);
            throw new BizException("执行输出方案失败,读取蓝图文件列表失败: " + e.getMessage());
        }

        if (blueprints.isEmpty()) {
            throw new BizException("执行输出方案失败,未找到匹配的蓝图文件.");
        }

        //使用第二代QBE引擎渲染并写出文件
        qbeVelocityEngine.setOutputBasePath(oPrjPath.toString());
        qbeVelocityEngine.setOverwriteEnabled(true);
        qbeVelocityEngine.render(qbeModel, opSchemaPo, blueprints);

        var message = "QBE生成引擎提交 OpSchemaId=" + opSchemaPo.getId() + " 蓝图数量=" + blueprints.size();

        //将输出目录推送到输出SCM
        scmService.pushToScm(outputScmPo, workSpaceOutputPath.toString(), message);

        //写入执行记录
        var endTime = LocalDateTime.now();
        var rcd = new OpRcdPo();
        rcd.setOpName(opSchemaPo.getName());
        rcd.setModelName(opSchemaPo.getModelName());
        rcd.setModelRemark(opSchemaPo.getModelRemark());
        rcd.setBizDomain(opSchemaPo.getBizDomain());
        rcd.setDsTableName(opSchemaPo.getTableName());
        rcd.setScmInputUrl(inputScmPo.getScmUrl());
        rcd.setScmOutputUrl(outputScmPo.getScmUrl());
        rcd.setStartTime(startTime);
        rcd.setEndTime(endTime);
        rcd.setDurationMs((int) Duration.between(startTime, endTime).toMillis());
        rcd.setCreatorUsername(session().getUsername());
        rcd.setDsName(ds.getName());
        rcd.setDsUrl(ds.getUrl());

        //QBE参数序列化
        rcd.setQbeParams(gson.toJson(qbeModel));
        opRcdRepository.save(rcd);
    }


    /**
     * 获取蓝图文件路径
     * 输入路径: 工作空间/input/输入SCM相对路径
     *
     * @param opSchemaPo 输出方案
     * @return 蓝图文件绝对路径
     */
    private Path getBluePrintPath(OpSchemaPo opSchemaPo) {
        var workSpaceName = "gen_workspace_" + opSchemaPo.getInputScmId();
        var workSpacePath = attachService.getAttachLocalPath(Paths.get(workSpaceName));
        var workSpaceInputPath = workSpacePath.resolve("input");
        var iAppendPath = opSchemaPo.getBaseInput().trim();  //这是在输出方案中配置的蓝图SCM相对路径
        if (iAppendPath.startsWith("/")) {
            iAppendPath = iAppendPath.substring(1);
        }
        return workSpaceInputPath.resolve(iAppendPath);
    }


}