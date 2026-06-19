package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.commons.DatasourceConnectionManager;
import com.ksptool.bio.biz.aacp.commons.MicroFuncContextHolder;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.annotation.Param;
import com.ksptool.bio.biz.aacp.model.datasource.AacpDatasourcePo;
import com.ksptool.bio.biz.aacp.model.datasource.dto.AddAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.EditAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.GetAacpDatasourceListDto;
import com.ksptool.bio.biz.aacp.model.datasource.vo.DatasourceInfoVo;
import com.ksptool.bio.biz.aacp.model.datasource.vo.ExecuteResultVo;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceDetailsVo;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceListVo;
import com.ksptool.bio.biz.aacp.repository.AacpDatasourceRepository;
import com.ksptool.bio.biz.aacp.repository.AgentHubCapRepository;
import com.ksptool.bio.biz.aacp.repository.CapDatasourceRepository;
import com.ksptool.bio.commons.dataprocess.Str;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ksptool.entities.Entities.as;
import static com.ksptool.entities.Entities.assign;

@Service
public class AacpDatasourceService {

    @Autowired
    private AacpDatasourceRepository repository;

    @Autowired
    private CapDatasourceRepository capDatasourceRepository;

    @Autowired
    private AgentHubCapRepository agentHubCapRepository;

    @Autowired
    private DatasourceConnectionManager connectionManager;

    public PageResult<GetAacpDatasourceListVo> getAacpDatasourceList(GetAacpDatasourceListDto dto) {
        AacpDatasourcePo query = new AacpDatasourcePo();
        assign(dto, query);
        Page<AacpDatasourcePo> page = repository.getAacpDatasourceList(query, dto.pageRequest());
        if (page.isEmpty()) {
            return PageResult.successWithEmpty();
        }
        return PageResult.success(as(page.getContent(), GetAacpDatasourceListVo.class), (int) page.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addAacpDatasource(AddAacpDatasourceDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), null) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        repository.save(as(dto, AacpDatasourcePo.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public void editAacpDatasource(EditAacpDatasourceDto dto) throws BizException {
        if (repository.countByCodeExcludeId(dto.getCode(), dto.getId()) > 0) {
            throw new BizException("唯一编码已存在,请更换后重试.");
        }
        AacpDatasourcePo updatePo = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("更新失败,数据不存在或无权限访问."));

        String originalPassword = updatePo.getPassword();
        String originalUsername = updatePo.getUsername();
        assign(dto, updatePo);
        if (StringUtils.isBlank(dto.getPassword())) {
            updatePo.setPassword(originalPassword);
        }
        if (StringUtils.isBlank(dto.getUsername())) {
            updatePo.setUsername(originalUsername);
        }
        repository.save(updatePo);
        connectionManager.removeDataSource(dto.getId());
    }

    public GetAacpDatasourceDetailsVo getAacpDatasourceDetails(CommonIdDto dto) throws BizException {
        AacpDatasourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("查询详情失败,数据不存在或无权限访问."));
        return as(po, GetAacpDatasourceDetailsVo.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeAacpDatasource(CommonIdDto dto) throws BizException {
        if (dto.isBatch()) {
            throw new BizException("数据源不支持批量删除");
        }
        long refCount = capDatasourceRepository.countByDatasourceId(dto.getId());
        if (refCount > 0) {
            throw new BizException("该数据源已被" + refCount + "个能力包使用，无法删除");
        }
        repository.deleteById(dto.getId());
        connectionManager.removeDataSource(dto.getId());
    }

    public Result<String> testAacpDatasourceConnection(CommonIdDto dto) throws BizException {
        AacpDatasourcePo po = repository.findById(dto.getId())
                .orElseThrow(() -> new BizException("测试失败,数据不存在或无权限访问."));
        long start = System.currentTimeMillis();
        try {
            Class.forName(po.getDrive());
        } catch (ClassNotFoundException e) {
            return Result.error("测试失败,JDBC驱动不存在.");
        }
        try (Connection c = DriverManager.getConnection(po.getUrl(), po.getUsername(), po.getPassword())) {
        } catch (SQLException e) {
            return Result.error("测试失败,连接失败: " + e.getMessage());
        }
        return Result.success("成功连接数据库 耗时: " + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * 列出当前智能体可访问的所有数据源
     */
    @MicroFunc(target = "datasource.list_databases", name = "列出数据源", description = "获取当前智能体可访问的数据源列表，包含ID、名称和数据库类型")
    public List<DatasourceInfoVo> getMyDatabases() {
        Long hubId = MicroFuncContextHolder.get();
        if (hubId == null) {
            return List.of();
        }
        List<Long> capIds = agentHubCapRepository.getCapIdsByHubId(hubId);
        List<Long> dsIds = new ArrayList<>();
        for (Long capId : capIds) {
            dsIds.addAll(capDatasourceRepository.getDatasourceIdsByCapId(capId));
        }
        List<AacpDatasourcePo> allDs = repository.findAllById(dsIds);
        return allDs.stream().map(ds -> {
            DatasourceInfoVo vo = new DatasourceInfoVo();
            vo.setId(ds.getId());
            vo.setName(ds.getName());
            vo.setKind(ds.getKind());
            vo.setDefaultDb(ds.getDefaultDb());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 在指定数据源上执行 SQL 查询或更新
     */
    @MicroFunc(target = "datasource.execute_query", name = "执行SQL", description = "在指定数据源上执行SQL语句。SELECT返回数据集，INSERT/UPDATE/DELETE返回受影响行数。禁止混合多条SELECT语句。")
    public ExecuteResultVo executeQuery(@Param("dataSourceId") Long dataSourceId, @Param("sql") String sql) throws BizException {
        Long hubId = MicroFuncContextHolder.get();
        if (hubId == null) {
            throw new BizException("无法获取当前会话上下文");
        }
        List<Long> capIds = agentHubCapRepository.getCapIdsByHubId(hubId);
        boolean authorized = false;
        for (Long capId : capIds) {
            List<Long> dsIds = capDatasourceRepository.getDatasourceIdsByCapId(capId);
            if (dsIds.contains(dataSourceId)) {
                authorized = true;
                break;
            }
        }
        if (!authorized) {
            throw new BizException("无权限访问该数据源");
        }

        AacpDatasourcePo dsPo = repository.findById(dataSourceId)
                .orElseThrow(() -> new BizException("数据源不存在: " + dataSourceId));
        DataSource dataSource = connectionManager.getDataSource(dsPo);

        List<String> statements = Str.safeSplit(sql, ";");
        if (statements.isEmpty()) {
            ExecuteResultVo err = new ExecuteResultVo();
            err.setError(true);
            err.setMessage("未检测到任何SQL语句");
            return err;
        }

        List<String> selects = statements.stream().filter(s -> s.trim().toUpperCase().startsWith("SELECT")).collect(Collectors.toList());
        List<String> updates = statements.stream().filter(s -> !s.trim().toUpperCase().startsWith("SELECT")).collect(Collectors.toList());

        if (!selects.isEmpty() && !updates.isEmpty()) {
            ExecuteResultVo err = new ExecuteResultVo();
            err.setError(true);
            err.setMessage("不允许同时执行查询与更新操作，请拆分后分别提交");
            return err;
        }

        try {
            if (!selects.isEmpty()) {
                ExecuteResultVo result = new ExecuteResultVo();
                result.setSql(selects.get(0));
                int maxRows = dsPo.getQueryMaxRows();
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement(selects.get(0))) {
                    ps.setMaxRows(maxRows + 1);
                    try (ResultSet rs = ps.executeQuery()) {
                        ResultSetMetaData meta = rs.getMetaData();
                        int colCount = meta.getColumnCount();
                        List<String> cols = new ArrayList<>();
                        for (int i = 1; i <= colCount; i++) {
                            cols.add(meta.getColumnLabel(i));
                        }
                        result.setColumns(cols);
                        List<Map<String, Object>> rows = new ArrayList<>();
                        int rowCount = 0;
                        while (rs.next()) {
                            rowCount++;
                            if (rowCount > maxRows) {
                                result.setTruncated(true);
                                break;
                            }
                            Map<String, Object> row = new LinkedHashMap<>();
                            for (int i = 1; i <= colCount; i++) {
                                row.put(meta.getColumnLabel(i), rs.getObject(i));
                            }
                            rows.add(row);
                        }
                        result.setRows(rows);
                        result.setRowCount(rows.size());
                    }
                }
                result.setError(false);
                return result;
            }

            ExecuteResultVo result = new ExecuteResultVo();
            int totalAffected = 0;
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    for (String stmt : updates) {
                        try (PreparedStatement ps = conn.prepareStatement(stmt)) {
                            totalAffected += ps.executeUpdate();
                        }
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
            result.setError(false);
            result.setAffectedRows(totalAffected);
            result.setMessage("影响 " + totalAffected + " 行");
            return result;
        } catch (SQLException e) {
            ExecuteResultVo err = new ExecuteResultVo();
            err.setError(true);
            err.setMessage("SQL执行失败: " + e.getMessage());
            return err;
        }
    }
}