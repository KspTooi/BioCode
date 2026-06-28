DROP TABLE IF EXISTS core_attach_pool;
CREATE TABLE core_attach_pool(
                                 `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                                 `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                                 `pool_path` VARCHAR(2048) NOT NULL  COMMENT '存储池地址' ,
                                 `pool_capacity_bytes` BIGINT NOT NULL  COMMENT '总可用字节' ,
                                 `pool_usage_bytes` BIGINT NOT NULL  COMMENT '总已用字节' ,
                                 `pool_attaches_bytes` BIGINT   COMMENT '附件占用字节' ,
                                 `indexed_count` INT NOT NULL  COMMENT '已索引附件数' ,
                                 `indexed_lost_count` INT NOT NULL  COMMENT '失效索引数' ,
                                 `drift_count` INT NOT NULL  COMMENT '游离附件数' ,
                                 `scan_start_time` DATETIME NOT NULL  COMMENT '扫描开始时间' ,
                                 `scan_end_time` DATETIME   COMMENT '扫描结束时间' ,
                                 `scan_status` TINYINT NOT NULL  COMMENT '扫描状态 0:正在扫描 1:成功' ,
                                 `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                                 `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                                 `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                                 `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                                 PRIMARY KEY (id)
)  COMMENT = '附件池表';
