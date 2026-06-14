package com.ksptool.bio.biz.aacp.commons;

import com.ksptool.bio.biz.aacp.model.datasource.AacpDatasourcePo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源连接池管理器：按数据源 ID 缓存 HikariCP 连接池。
 * <p>
 * 微函数执行 SQL 时从此获取连接池，编辑/删除数据源后应调用 removeDataSource 清理。
 */
@Slf4j
@Component
public class DatasourceConnectionManager {

    private final Map<Long, HikariDataSource> poolMap = new ConcurrentHashMap<>();

    /**
     * 获取或创建数据源连接池
     */
    public DataSource getDataSource(AacpDatasourcePo po) {
        Long id = po.getId();
        HikariDataSource existing = poolMap.get(id);
        if (existing != null && !existing.isClosed()) {
            return existing;
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(po.getUrl());
        config.setUsername(po.getUsername());
        config.setPassword(po.getPassword());
        config.setDriverClassName(po.getDrive());
        config.setMaximumPoolSize(3);
        config.setConnectionTimeout(5000);
        config.setPoolName("ds-" + id);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        HikariDataSource ds = new HikariDataSource(config);
        poolMap.put(id, ds);
        log.info("创建数据源连接池: id={} name={}", id, po.getName());
        return ds;
    }

    /**
     * 移除并关闭指定数据源的连接池
     */
    public void removeDataSource(Long id) {
        HikariDataSource ds = poolMap.remove(id);
        if (ds != null && !ds.isClosed()) {
            ds.close();
            log.info("关闭数据源连接池: id={}", id);
        }
    }

    @PreDestroy
    public void destroy() {
        poolMap.values().forEach(ds -> {
            if (!ds.isClosed()) {
                ds.close();
            }
        });
        poolMap.clear();
    }
}