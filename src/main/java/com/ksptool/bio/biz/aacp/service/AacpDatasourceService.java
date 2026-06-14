package com.ksptool.bio.biz.aacp.service;

import com.ksptool.assembly.entity.exception.BizException;
import com.ksptool.assembly.entity.web.CommonIdDto;
import com.ksptool.assembly.entity.web.PageResult;
import com.ksptool.assembly.entity.web.Result;
import com.ksptool.bio.biz.aacp.commons.DatasourceConnectionManager;
import com.ksptool.bio.biz.aacp.commons.MicroFuncContextHolder;
import com.ksptool.bio.biz.aacp.commons.annotation.MicroFunc;
import com.ksptool.bio.biz.aacp.commons.annotation.Param;
import com.ksptool.bio.biz.aacp.model.AacpAgentHubCapPo;
import com.ksptool.bio.biz.aacp.model.datasource.AacpDatasourcePo;
import com.ksptool.bio.biz.aacp.model.datasource.dto.AddAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.EditAacpDatasourceDto;
import com.ksptool.bio.biz.aacp.model.datasource.dto.GetAacpDatasourceListDto;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceDetailsVo;
import com.ksptool.bio.biz.aacp.model.datasource.vo.GetAacpDatasourceListVo;
import com.ksptool.bio.biz.aacp.repository.AacpDatasourceRepository;
import com.ksptool.bio.biz.aacp.repository.AgentHubCapRepository;
import com.ksptool.bio.biz.aacp.repository.CapDatasourceRepository;
import lombok.Getter;
import lombok.Setter;
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

        //连接池配置可能变更，移除旧连接池
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
            // 连接成功即自动关闭
        } catch (SQLException e) {
            return Result.error("测试失败,连接失败: " + e.getMessage());
        }
        return Result.success("成功连接数据库 耗时: " + (System.currentTimeMillis() - start) + "ms");
    }


    /**
     * 列出当前智能体可访问的所有数据源
     */
    @MicroFunc(target = "datasource.list_databases", name = "列出数据源", description = "获取当前智能体可访问的数据源列表，包含ID、名称和数据库类型")
    public List<DatasourceInfo> getMyDatabases() {
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
            DatasourceInfo info = new DatasourceInfo();
            info.setId(ds.getId());
            info.setName(ds.getName());
            info.setKind(ds.getKind());
            info.setDefaultDb(ds.getDefaultDb());
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * 在指定数据源上执行 SQL 查询或更新
     */
    @MicroFunc(target = "datasource.execute_query", name = "执行SQL", description = "在指定数据源上执行SQL语句。SELECT返回数据集，INSERT/UPDATE/DELETE返回受影响行数。禁止混合多条SELECT语句。")
    public QueryResult executeQuery(@Param("dataSourceId") Long dataSourceId, @Param("sql") String sql) throws BizException {
        //权限校验
        checkDataSourceAccess(dataSourceId);

        // 2. 获取数据源连接池
        AacpDatasourcePo dsPo = repository.findById(dataSourceId)
                .orElseThrow(() -> new BizException("数据源不存在: " + dataSourceId));
        DataSource dataSource = connectionManager.getDataSource(dsPo);

        // 3. SQL 安全：多语句拆分
        List<String> statements = splitSql(sql);
        if (statements.isEmpty()) {
            QueryResult err = new QueryResult();
            err.setError(true);
            err.setMessage("未检测到任何SQL语句");
            return err;
        }

        // 4. 分类：所有 SELECT 语句
        List<String> selects = statements.stream().filter(s -> isSelect(s)).collect(Collectors.toList());
        List<String> updates = statements.stream().filter(s -> !isSelect(s)).collect(Collectors.toList());

        // 混合 SELECT 和非 SELECT → 拒绝
        if (!selects.isEmpty() && !updates.isEmpty()) {
            QueryResult err = new QueryResult();
            err.setError(true);
            err.setMessage("不允许同时执行查询与更新操作，请拆分后分别提交");
            return err;
        }

        // 批量 SELECT → 只执行第一条
        if (selects.size() > 1) {
            statements = List.of(selects.get(0));
        }

        try {
            if (!selects.isEmpty()) {
                return executeSelect(dataSource, statements.get(0), dsPo.getQueryMaxRows());
            }
            return executeUpdate(dataSource, statements);
        } catch (SQLException e) {
            QueryResult err = new QueryResult();
            err.setError(true);
            err.setMessage("SQL执行失败: " + e.getMessage());
            return err;
        }
    }

    // ============ 权限校验 ============

    private void checkDataSourceAccess(Long dataSourceId) throws BizException {
        Long hubId = MicroFuncContextHolder.get();
        if (hubId == null) {
            throw new BizException("无法获取当前会话上下文");
        }
        List<Long> capIds = agentHubCapRepository.getCapIdsByHubId(hubId);
        for (Long capId : capIds) {
            List<Long> dsIds = capDatasourceRepository.getDatasourceIdsByCapId(capId);
            if (dsIds.contains(dataSourceId)) {
                return;
            }
        }
        throw new BizException("无权限访问该数据源");
    }

    // ============ SQL 工具 ============

    private List<String> splitSql(String sql) {
        List<String> result = new ArrayList<>();
        for (String stmt : sql.split(";")) {
            String trimmed = stmt.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private boolean isSelect(String sql) {
        return sql.trim().toUpperCase().startsWith("SELECT");
    }

    // ============ SQL 执行 ============

    private QueryResult executeSelect(DataSource dataSource, String sql, int maxRows) throws SQLException {
        QueryResult result = new QueryResult();
        result.setSql(sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setMaxRows(maxRows + 1); // 多取一行用于判断是否被截断
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(meta.getColumnLabel(i));
                }
                result.setColumns(columns);

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

    private QueryResult executeUpdate(DataSource dataSource, List<String> statements) throws SQLException {
        QueryResult result = new QueryResult();
        int totalAffected = 0;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String sql : statements) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
    }

    // ============ 响应值对象 ============

    @Getter
    @Setter
    public static class DatasourceInfo {
        private Long id;
        private String name;
        private Integer kind;
        private String defaultDb;
    }

    @Getter
    @Setter
    public static class QueryResult {
        private boolean error;
        private String message;
        private String sql;
        private List<String> columns;
        private List<Map<String, Object>> rows;
        private int rowCount;
        private boolean truncated;
        private int affectedRows;
    }
}