DROP TABLE IF EXISTS assembly_op_rcd;
CREATE TABLE assembly_op_rcd(
                                `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                                `op_name` VARCHAR(32) NOT NULL  COMMENT '输出方案名称' ,
                                `ds_name` VARCHAR(32) NOT NULL  COMMENT '数据源名称' ,
                                `ds_table_name` VARCHAR(80) NOT NULL  COMMENT '数据源表名' ,
                                `ds_url` TEXT NOT NULL  COMMENT '数据源连接字符串' ,
                                `scm_input_url` VARCHAR(2048) NOT NULL  COMMENT '输入SCM仓库地址' ,
                                `scm_output_url` VARCHAR(2048) NOT NULL  COMMENT '输出SCM仓库地址' ,
                                `model_name` VARCHAR(255) NOT NULL  COMMENT '模型名称' ,
                                `model_remark` VARCHAR(80) NOT NULL  COMMENT '模型备注' ,
                                `biz_domain` VARCHAR(80) NOT NULL  COMMENT '业务域' ,
                                `qbe_params` JSON NOT NULL  COMMENT 'QBE参数' ,
                                `start_time` DATETIME NOT NULL  COMMENT '开始时间' ,
                                `end_time` DATETIME NOT NULL  COMMENT '结束时间' ,
                                `duration_ms` INT NOT NULL  COMMENT '耗时MS' ,
                                `creator_username` VARCHAR(80) NOT NULL  COMMENT '操作人账号' ,
                                `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                                `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                                `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                                `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                                `delete_time` DATETIME   COMMENT '删除时间' ,
                                PRIMARY KEY (id)
)  COMMENT = '输出方案执行记录';
