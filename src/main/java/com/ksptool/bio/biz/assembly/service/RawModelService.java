package com.ksptool.bio.biz.assembly.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.bio.biz.assembly.model.datsource.DataSourcePo;
import com.ksptool.bio.biz.assembly.model.opschema.OpSchemaPo;
import com.ksptool.bio.biz.assembly.model.rawmodel.RawModelPo;
import com.ksptool.bio.biz.assembly.model.rawmodel.dto.AddRawModelDto;
import com.ksptool.bio.biz.assembly.model.rawmodel.dto.EditRawModelDto;
import com.ksptool.bio.biz.assembly.model.rawmodel.dto.GetRawModelDto;
import com.ksptool.bio.biz.assembly.model.rawmodel.vo.GetRawModelDetailsVo;
import com.ksptool.bio.biz.assembly.model.rawmodel.vo.GetRawModelListVo;
import com.ksptool.bio.biz.assembly.repository.DataSourceRepository;
import com.ksptool.bio.biz.assembly.repository.OpSchemaRepository;
import com.ksptool.bio.biz.assembly.repository.RawModelRepository;
import com.ksptool.entities.any.Any;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Service
public class RawModelService {

    //没有长度的原始数据类型(只针对mysql数据库 长度为NULL)
    private static final List<String> NO_LENGTH_DATA_TYPES = Arrays.asList(
            // 整数类型
            "TINYINT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER", "BIGINT",
            // 浮点/精确数值类型
            "FLOAT", "DOUBLE", "REAL", "DECIMAL", "NUMERIC",
            // 日期/时间类型
            "DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR",
            // 大文本/大二进制类型
            "TINYTEXT", "TEXT", "MEDIUMTEXT", "LONGTEXT",
            "TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB",
            // JSON / 空间类型
            "JSON", "GEOMETRY", "POINT", "LINESTRING", "POLYGON",
            "MULTIPOINT", "MULTILINESTRING", "MULTIPOLYGON", "GEOMETRYCOLLECTION",
            // 布尔
            "BOOLEAN", "BOOL"
    );

    @Autowired
    private RawModelRepository repository;

    @Autowired
    private OpSchemaRepository opSchemaRepository;

    @Autowired
    private DataSourceRepository dataSourceRepository;


    /**
     * 查询原始模型列表
     *
     * @param dto 查询参数
     * @return 原始模型列表
     */
    public PageResult<GetRawModelListVo> getRawModelList(GetRawModelDto dto) {
        RawModelPo query = new RawModelPo();
        assign(dto, query);

        List<RawModelPo> list = repository.getRawModelList(query);
        if (list.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetRawModelListVo> vos = as(list, GetRawModelListVo.class);
        return PageResult.success(vos, list.size());
    }

    /**
     * 新增原始模型
     *
     * @param dto 新增参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRawModel(AddRawModelDto dto) {
        RawModelPo insertPo = as(dto, RawModelPo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑原始模型
     *
     * @param dto 编辑参数
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void editRawModel(EditRawModelDto dto) throws BizException {
        RawModelPo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        assign(dto, updatePo);
        repository.save(updatePo);
    }

    /**
     * 查询原始模型详情
     *
     * @param dto 查询参数
     * @return 原始模型详情
     * @throws BizException 业务异常
     */
    public GetRawModelDetailsVo getRawModelDetails(CommonIdDto dto) throws BizException {
        RawModelPo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetRawModelDetailsVo.class);
    }

    /**
     * 删除原始模型元素
     *
     * @param dto 删除参数
     * @throws BizException 业务异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRawModel(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            repository.deleteAllById(dto.getIds());
            return;
        }
        repository.deleteById(dto.getId());
    }


    /**
     * 从数据源同步原始模型
     *
     * @param opSchemaId 输出方案ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncRawModelFromDataSource(Long opSchemaId) throws BizException {

        //查询输出方案
        OpSchemaPo opSchema = opSchemaRepository.findById(opSchemaId)
                .orElseThrow(() -> new BizException("输出方案不存在或无权限访问."));

        //查询数据源
        var hasBindDataSource = opSchema.getDataSourceId() != null;

        if (!hasBindDataSource) {
            throw new BizException("输出方案未绑定数据源,请先绑定数据源.");
        }

        var dataSource = dataSourceRepository.findById(opSchema.getDataSourceId())
                .orElseThrow(() -> new BizException("数据源不存在或无权限访问."));

        //查询数据源表字段
        var fields = getDataSourceTableFields(dataSource, opSchema.getTableName());

        if (fields == null) {
            throw new BizException("查询数据源表字段失败.");
        }

        //先删除输出方案的原始模型
        repository.clearRawModelByOutputSchemaId(opSchemaId);

        //批量组装原始模型
        var rawModels = Any.of(fields).val("outputSchemaId", opSchemaId).asList(RawModelPo.class);

        //批量保存原始模型
        repository.saveAll(rawModels);

        //更新输出方案字段数量
        opSchema.setFieldCountOrigin(fields.size());

        //保存输出方案
        opSchemaRepository.save(opSchema);
    }

    /**
     * 查询数据源表字段
     *
     * @param dataSource 数据源
     * @return 数据源表字段 获取异常时直接返回null
     */
    public List<RawModelPo> getDataSourceTableFields(DataSourcePo dataSource, String tableName) {

        var drive = dataSource.getDrive();
        var url = dataSource.getUrl();
        var username = dataSource.getUsername();
        var password = dataSource.getPassword();
        var dbSchema = dataSource.getDbSchema();

        try {
            Class.forName(drive);
        } catch (ClassNotFoundException e) {
            return null;
        }

        if (tableName == null || tableName.isBlank()) {
            return null;
        }

        List<RawModelPo> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 查询主键列表
            Set<String> pkColumns = new HashSet<>();
            try (ResultSet pkRs = metaData.getPrimaryKeys(dbSchema, dbSchema, tableName)) {
                while (pkRs.next()) {
                    pkColumns.add(pkRs.getString("COLUMN_NAME"));
                }
            }

            try (ResultSet rs = metaData.getColumns(dbSchema, dbSchema, tableName, "%")) {
                int seq = 1;
                while (rs.next()) {
                    RawModelPo po = new RawModelPo();
                    po.setId(null);
                    po.setOutputSchemaId(null);
                    po.setName(rs.getString("COLUMN_NAME"));
                    po.setDataType(rs.getString("TYPE_NAME"));
                    po.setLength(null);
                    po.setRequire(0); //是否必填 0:否 1:是
                    po.setRemark(rs.getString("REMARKS"));
                    po.setPk(0); //是否主键 0:否 1:是
                    po.setSeq(seq++);

                    //处理字段长度
                    int columnSize = rs.getInt("COLUMN_SIZE");
                    boolean noLength = NO_LENGTH_DATA_TYPES.contains(po.getDataType().toUpperCase());

                    if (columnSize > 0 && !noLength) {
                        po.setLength(columnSize);
                    }

                    //处理字段必填项
                    String nullable = rs.getString("IS_NULLABLE");

                    if ("NO".equalsIgnoreCase(nullable)) {
                        po.setRequire(1);
                    }

                    //处理字段主键项
                    boolean isPk = pkColumns.contains(po.getName());

                    if (isPk) {
                        po.setPk(1);
                        po.setRequire(1); //主键是必填项
                    }

                    result.add(po);
                }
            }
        } catch (SQLException e) {
            return null;
        }

        return result;
    }

}