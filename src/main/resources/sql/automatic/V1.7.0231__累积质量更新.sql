DROP TABLE IF EXISTS qf_model_group;
CREATE TABLE qf_model_group(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `name` VARCHAR(80) NOT NULL  COMMENT '组名称' ,
    `code` VARCHAR(32) NOT NULL  COMMENT '组编码' ,
    `remark` VARCHAR(500)   COMMENT '备注' ,
    `seq` INT NOT NULL  COMMENT '排序' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间 NULL未删' ,
    PRIMARY KEY (id)
)  COMMENT = '流程模型分组';
DROP TABLE IF EXISTS qf_model;
CREATE TABLE qf_model(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `active_deploy_id` BIGINT   COMMENT '该模型生效的部署ID' ,
    `group_id` BIGINT   COMMENT '模型组ID' ,
    `form_id` BIGINT   COMMENT '关联表单ID' ,
    `name` VARCHAR(80) NOT NULL  COMMENT '模型名称' ,
    `code` VARCHAR(32) NOT NULL  COMMENT '模型编码' ,
    `bpmn_xml` LONGTEXT   COMMENT '模型BPMN XML' ,
    `version` INT NOT NULL  COMMENT '模型版本号' ,
    `status` TINYINT NOT NULL  COMMENT '模型状态 0:草稿 1:已部署 2:历史' ,
    `seq` INT NOT NULL  COMMENT '排序' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间 NULL未删' ,
    PRIMARY KEY (id)
)  COMMENT = '流程模型';
DROP TABLE IF EXISTS qf_model_deploy_rcd;
CREATE TABLE qf_model_deploy_rcd(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `model_id` BIGINT NOT NULL  COMMENT '模型ID' ,
    `form_id` BIGINT NOT NULL  COMMENT '关联表单ID' ,
    `name` VARCHAR(80) NOT NULL  COMMENT '模型名称' ,
    `code` VARCHAR(32) NOT NULL  COMMENT '模型编码' ,
    `bpmn_xml` LONGTEXT   COMMENT '模型BPMN XML' ,
    `version` INT NOT NULL  COMMENT '模型版本号' ,
    `eng_deployment_id` VARCHAR(200)   COMMENT '引擎部署ID(部署失败为NULL)' ,
    `eng_process_def_id` VARCHAR(200)   COMMENT '引擎流程ID(部署失败为NULL)' ,
    `eng_deploy_result` VARCHAR(2000) NOT NULL  COMMENT '引擎返回的部署结果' ,
    `status` TINYINT NOT NULL  COMMENT '部署状态 0:正常 1:部署失败' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间 NULL未删' ,
    PRIMARY KEY (id)
)  COMMENT = '流程模型部署历史';
DROP TABLE IF EXISTS qf_biz_form;
CREATE TABLE qf_biz_form(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `name` VARCHAR(40) NOT NULL  COMMENT '业务名称(如采购申请)' ,
    `code` VARCHAR(16) NOT NULL  COMMENT '业务编码(如PURCHASE_APPLY, 必须全局唯一)' ,
    `form_type` TINYINT NOT NULL  COMMENT '表单类型 0:手搓表单 1:动态表单' ,
    `icon` VARCHAR(80) NOT NULL  COMMENT '图标' ,
    `table_name` VARCHAR(200) NOT NULL  COMMENT '对应物理表名' ,
    `route_pc` VARCHAR(512)   COMMENT 'PC端路由名' ,
    `route_mobile` VARCHAR(512)   COMMENT '移动端路由名' ,
    `status` TINYINT NOT NULL  COMMENT '状态 0:正常 1:停用' ,
    `seq` INT NOT NULL  COMMENT '排序' ,
    `summary_template` VARCHAR(200)   COMMENT '摘要模板' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间' ,
    PRIMARY KEY (id)
)  COMMENT = '业务表单表';
DROP TABLE IF EXISTS qf_todo;
CREATE TABLE qf_todo(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `eng_task_id` VARCHAR(200) NOT NULL  COMMENT '引擎任务ID' ,
    `eng_proc_id` VARCHAR(200) NOT NULL  COMMENT '引擎流程ID' ,
    `biz_form_id` BIGINT NOT NULL  COMMENT '业务表单ID' ,
    `eng_process_def_id` VARCHAR(200)   COMMENT '引擎流程ID' ,
    `table_name` VARCHAR(200) NOT NULL  COMMENT '物理表名(带入业务表单数据)' ,
    `data_id` BIGINT NOT NULL  COMMENT '物理表数据主键ID' ,
    `node_name` VARCHAR(80) NOT NULL  COMMENT '当前节点名称 (如: 财务总监审批)' ,
    `summary` VARCHAR(500) NOT NULL  COMMENT '摘要(如：张三提交的 5000 元报销)' ,
    `member_type` TINYINT NOT NULL  COMMENT '办理成员类型 0:办理人, 1:候选组' ,
    `member_id` BIGINT NOT NULL  COMMENT '办理成员ID (用户ID或用户组标识)' ,
    `initiator_id` BIGINT NOT NULL  COMMENT '发起人ID' ,
    `initiator_name` VARCHAR(20) NOT NULL  COMMENT '发起人名' ,
    `initiator_time` DATETIME NOT NULL  COMMENT '发起时间' ,
    `status` TINYINT NOT NULL  COMMENT '待办状态 0:待办 1:已办 10:已作废' ,
    `fin_member_id` BIGINT   COMMENT '实际办理人ID' ,
    `fin_member_name` VARCHAR(20)   COMMENT '实际办理人姓名' ,
    `fin_time` DATETIME   COMMENT '实际办理时间' ,
    `action` TINYINT   COMMENT '操作 0:同意 1:驳回' ,
    `comment` VARCHAR(500)   COMMENT '办理人意见' ,
    `duration` BIGINT   COMMENT '耗时(毫秒)' ,
    `create_time` DATETIME NOT NULL  COMMENT '任务到达时间' ,
    `delete_time` DATETIME   COMMENT '删除时间' ,
    PRIMARY KEY (id)
)  COMMENT = '待办事项表';
DROP TABLE IF EXISTS qf_cc;
CREATE TABLE qf_cc(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
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
