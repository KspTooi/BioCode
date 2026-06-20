DROP TABLE IF EXISTS assembly_poly_template;
CREATE TABLE assembly_poly_template(
                                       `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                                       `name` VARCHAR(40) NOT NULL  COMMENT '模板名称' ,
                                       `code` VARCHAR(16) NOT NULL  COMMENT '模板代码' ,
                                       `seq` TINYINT NOT NULL  COMMENT '排序' ,
                                       `status` TINYINT NOT NULL  COMMENT '状态 0:禁用 1:启用' ,
                                       `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                                       `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                                       `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                                       `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                                       `delete_time` DATETIME   COMMENT '删除时间' ,
                                       PRIMARY KEY (id)
)  COMMENT = '聚合模板';

DROP TABLE IF EXISTS assembly_poly_template_field;
CREATE TABLE assembly_poly_template_field(
                                             `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                                             `poly_template_id` BIGINT NOT NULL  COMMENT '聚合模板ID' ,
                                             `name` VARCHAR(255) NOT NULL  COMMENT '字段名' ,
                                             `policy_crud_json` JSON NOT NULL  COMMENT '可见性策略 ADD、EDIT、LIST_QUERY、LIST_VIEW' ,
                                             `policy_query` TINYINT NOT NULL  COMMENT '查询策略 0:等于' ,
                                             `policy_view` TINYINT NOT NULL  COMMENT '显示策略 0:文本框 1:文本域 2:下拉 3:单 4:多 5:LD 6:LDT' ,
                                             `seq` INT NOT NULL  COMMENT '排序' ,
                                             `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                                             `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                                             `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                                             `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                                             `delete_time` DATETIME   COMMENT '删除时间' ,
                                             PRIMARY KEY (id)
)  COMMENT = '聚合模板字段';