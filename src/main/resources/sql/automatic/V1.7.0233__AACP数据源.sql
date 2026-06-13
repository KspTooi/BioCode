DROP TABLE IF EXISTS aacp_datasource;
CREATE TABLE aacp_datasource(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `name` VARCHAR(40) NOT NULL  COMMENT '数据源名称' ,
    `code` VARCHAR(32) NOT NULL  COMMENT '数据源编码' ,
    `kind` TINYINT NOT NULL  COMMENT '数据源类型 0:MYSQL' ,
    `drive` VARCHAR(200) NOT NULL  COMMENT 'JDBC驱动' ,
    `url` TEXT NOT NULL  COMMENT '连接字符串' ,
    `username` VARCHAR(200)   COMMENT '连接用户名' ,
    `password` VARCHAR(2000)   COMMENT '连接密码' ,
    `default_db` VARCHAR(200) NOT NULL  COMMENT '默认数据库' ,
    `query_max_rows` INT NOT NULL  COMMENT '最大查询行数' ,
    `execute_batch` TINYINT NOT NULL  COMMENT '是否支持批处理 0:不支持 1:支持' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间' ,
    PRIMARY KEY (id)
)  COMMENT = '数据源表';

DROP TABLE IF EXISTS aacp_cap_datasource;
CREATE TABLE aacp_cap_datasource(
    `cap_id` BIGINT NOT NULL  COMMENT 'CID' ,
    `datasource_id` BIGINT NOT NULL  COMMENT 'DID' ,
    PRIMARY KEY (cap_id,datasource_id)
)  COMMENT = 'CD表';
