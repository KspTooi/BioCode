DROP TABLE IF EXISTS qf_cc;
CREATE TABLE qf_cc(
                      `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                      `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                      `dept_id` BIGINT NOT NULL  COMMENT '部门ID' ,
                      `eng_proc_id` VARCHAR(200) NOT NULL  COMMENT '引擎流程ID' ,
                      `biz_form_id` BIGINT NOT NULL  COMMENT '业务表单ID' ,
                      `table_name` VARCHAR(200) NOT NULL  COMMENT '物理表名(带入业务表单数据)' ,
                      `data_id` BIGINT NOT NULL  COMMENT '物理表数据主键ID' ,
                      `node_name` VARCHAR(80) NOT NULL  COMMENT '当前节点名称 (如: 财务总监审批)' ,
                      `summary` VARCHAR(500) NOT NULL  COMMENT '摘要(如：张三提交的 5000 元报销)' ,
                      `from_id` BIGINT   COMMENT '抄送发起人ID(自动抄送为null)' ,
                      `from_name` VARCHAR(20)   COMMENT '抄送发起人姓名' ,
                      `target_id` BIGINT NOT NULL  COMMENT '被抄送人ID' ,
                      `is_read` TINYINT NOT NULL  COMMENT '是否读 0:未读 1:已读' ,
                      `read_time` DATETIME   COMMENT '读取时间' ,
                      `create_time` DATETIME NOT NULL  COMMENT '抄送时间' ,
                      `delete_time` DATETIME   COMMENT '删除时间' ,
                      PRIMARY KEY (id)
)  COMMENT = '抄送表';
