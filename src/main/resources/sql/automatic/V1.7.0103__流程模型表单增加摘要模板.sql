ALTER TABLE qf_biz_form
    ADD COLUMN summary_template VARCHAR(200) COMMENT '摘要模板' AFTER seq;

DROP TABLE IF EXISTS qf_biz_form_field;
CREATE TABLE qf_biz_form_field(
                                  `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                                  `form_id` BIGINT NOT NULL  COMMENT '业务表ID' ,
                                  `field_name` VARCHAR(32) NOT NULL  COMMENT '字段名' ,
                                  `remark` VARCHAR(32) NOT NULL  COMMENT '备注' ,
                                  `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                                  `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                                  `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                                  `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                                  `delete_time` DATETIME   COMMENT '删除时间' ,
                                  PRIMARY KEY (id)
)  COMMENT = '业务表-字段配置';
