package com.ksptool.bio.biz.assembly.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.assembly.model.datsource.DataSourcePo;
import com.ksptool.bio.biz.assembly.model.datsource.dto.AddDataSourceDto;
import com.ksptool.bio.biz.assembly.model.datsource.dto.EditDataSourceDto;
import com.ksptool.bio.biz.assembly.model.datsource.dto.GetDataSourceListDto;
import com.ksptool.bio.biz.assembly.model.datsource.vo.GetDataSourceDetailsVo;
import com.ksptool.bio.biz.assembly.model.datsource.vo.GetDataSourceListVo;
import com.ksptool.bio.biz.assembly.model.datsource.vo.GetDataSourceTableListVo;
import com.ksptool.bio.biz.assembly.repository.DataSourceRepository;
import com.ksptool.bio.biz.assembly.repository.OpSchemaRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;


/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Service
public class DataSourceService {

    @Autowired
    private DataSourceRepository repository;

    @Autowired
    private OpSchemaRepository opSchemaRepository;

    /**
     * 查询数据源列表
     *
     * @param dto 查询参数
     * @return 数据源列表
     */
    public PageResult<GetDataSourceListVo> getDataSourceList(GetDataSourceListDto dto) {
        DataSourcePo query = new DataSourcePo();
        assign(dto, query);

        Page<DataSourcePo> page = repository.getDataSourceList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }

        List<GetDataSourceListVo> vos = as(page.getContent(), GetDataSourceListVo.class);
        return PageResult.success(vos, (int) page.getTotalElements());
    }

    /**
     * 新增数据源
     *
     * @param dto 新增参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void addDataSource(AddDataSourceDto dto) throws BizException {

        //校验编码是否唯一
        if (repository.countByCode(dto.getCode()) > 0) {
            throw new BizException("编码已存在:[" + dto.getCode() + "]");
        }

        DataSourcePo insertPo = as(dto, DataSourcePo.class);
        repository.save(insertPo);
    }

    /**
     * 编辑数据源
     *
     * @param dto 编辑参数
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void editDataSource(EditDataSourceDto dto) throws BizException {
        DataSourcePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        //校验编码是否唯一
        if (repository.countByCodeExcludeId(dto.getCode(), updatePo.getId()) > 0) {
            throw new BizException("编码已存在:[" + dto.getCode() + "]");
        }

        //保存旧的用户名和密码
        var oldUsername = updatePo.getUsername();
        var oldPassword = updatePo.getPassword();

        assign(dto, updatePo);

        //如果用户名为空则不修改
        if (StringUtils.isBlank(dto.getUsername())) {
            updatePo.setUsername(oldUsername);
        }

        //如果密码为空则不修改
        if (StringUtils.isBlank(dto.getPassword())) {
            updatePo.setPassword(oldPassword);
        }

        repository.save(updatePo);
    }

    /**
     * 查询数据源详情
     *
     * @param dto 查询参数
     * @return 数据源详情
     * @throws BizException
     */
    public GetDataSourceDetailsVo getDataSourceDetails(CommonIdDto dto) throws BizException {
        DataSourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetDataSourceDetailsVo.class);
    }

    /**
     * 删除数据源
     *
     * @param dto 删除参数
     * @throws BizException
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeDataSource(CommonIdDto dto) throws BizException {

        //删除时检查是否被输出方案使用
        if (opSchemaRepository.countByDataSourceId(dto.getId()) > 0) {
            throw new BizException("该数据源已被输出方案使用,无法执行删除操作!");
        }

        repository.deleteById(dto.getId());
    }

    /**
     * 测试数据源连接
     *
     * @param dto 查询参数
     * @return 数据源详情
     * @throws BizException
     */
    public Result<String> testDataSourceConnection(CommonIdDto dto) throws BizException {
        DataSourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("测试失败,数据不存在或无权限访问."));

        long startTime = System.currentTimeMillis();
        try {
            Class.forName(po.getDrive());
        } catch (ClassNotFoundException e) {
            return Result.error("测试失败,JDBC驱动不存在.");
        }

        try {
            Connection connection = DriverManager.getConnection(po.getUrl(), po.getUsername(), po.getPassword());
            connection.close();
        } catch (SQLException e) {
            return Result.error("测试失败,连接失败: " + e.getMessage());
        }

        return Result.success("成功连接数据库 耗时: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * 查询数据源中的所有表名和注释
     *
     * @param dto 查询参数
     * @return 表列表
     * @throws BizException
     */
    public List<GetDataSourceTableListVo> getDataSourceTableList(CommonIdDto dto) throws BizException {
        DataSourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询失败,数据不存在或无权限访问."));

        try {
            Class.forName(po.getDrive());
        } catch (ClassNotFoundException e) {
            throw new BizException("查询失败,JDBC驱动不存在.");
        }

        List<GetDataSourceTableListVo> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(po.getUrl(), po.getUsername(), po.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(po.getDbSchema(), po.getDbSchema(), "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    GetDataSourceTableListVo vo = new GetDataSourceTableListVo();
                    vo.setTableName(rs.getString("TABLE_NAME"));
                    vo.setTableComment(rs.getString("REMARKS"));
                    result.add(vo);
                }
            }
        } catch (SQLException e) {
            throw new BizException("查询失败,连接失败: " + e.getMessage());
        }

        return result;
    }


}