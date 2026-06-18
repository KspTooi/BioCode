DROP TABLE IF EXISTS aacp_micro_func;
CREATE TABLE aacp_micro_func(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `name` VARCHAR(40) NOT NULL  COMMENT '微函数名称' ,
    `code` VARCHAR(32) NOT NULL  COMMENT '微函数标识' ,
    `description` VARCHAR(1000) NOT NULL  COMMENT '意图词' ,
    `schema` JSON   COMMENT '入参规范' ,
    `target` VARCHAR(1000) NOT NULL  COMMENT '调用目标Bean' ,
    `remark` VARCHAR(500)   COMMENT '备注' ,
    `is_bundle` TINYINT NOT NULL  COMMENT '是否绑定 0:否 1:是' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间' ,
    PRIMARY KEY (id)
)  COMMENT = '微函数';
