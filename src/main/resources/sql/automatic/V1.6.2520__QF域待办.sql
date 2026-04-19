-- ==========================================================
-- 1. 删除旧的备份表 (如果存在)
-- ==========================================================
DROP TABLE IF EXISTS `zremovred_qf_todo`;

-- ==========================================================
-- 2. 重命名当前表为备份表
-- ==========================================================
ALTER TABLE `qf_todo` RENAME TO `zremovred_qf_todo`;

-- ==========================================================
-- 3. 创建新表
-- ==========================================================
CREATE TABLE `qf_todo` (
                           `id` BIGINT NOT NULL COMMENT '主键ID',
                           `root_id` BIGINT NOT NULL COMMENT '租户ID',
                           `dept_id` BIGINT NOT NULL COMMENT '部门ID',
                           `eng_task_id` VARCHAR(200) NOT NULL COMMENT '引擎任务ID',
                           `eng_proc_id` VARCHAR(200) NOT NULL COMMENT '引擎流程ID',
                           `biz_form_id` BIGINT NOT NULL COMMENT '业务表单ID',
                           `table_name` VARCHAR(200) NOT NULL COMMENT '物理表名(带入业务表单数据)',
                           `data_id` BIGINT NOT NULL COMMENT '物理表数据主键ID',
                           `node_name` VARCHAR(80) NOT NULL COMMENT '当前节点名称 (如: 财务总监审批)',
                           `summary` VARCHAR(500) NOT NULL COMMENT '摘要(如：张三提交的 5000 元报销)',
                           `member_type` TINYINT NOT NULL COMMENT '办理成员类型 0:办理人, 1:候选组',
                           `member_id` BIGINT NOT NULL COMMENT '办理成员ID (用户ID或用户组标识)',
                           `initiator_id` BIGINT NOT NULL COMMENT '发起人ID',
                           `initiator_name` VARCHAR(20) NOT NULL COMMENT '发起人名',
                           `initiator_time` DATETIME NOT NULL COMMENT '发起时间',
                           `status` TINYINT NOT NULL COMMENT '待办状态 0:待办 1:已办 10:已作废',
                           `fin_member_id` BIGINT COMMENT '实际办理人ID',
                           `fin_member_name` VARCHAR(20) COMMENT '实际办理人姓名',
                           `fin_time` DATETIME COMMENT '实际办理时间',
                           `action` TINYINT COMMENT '操作 0:同意 1:驳回',
                           `comment` VARCHAR(500) COMMENT '办理人意见',
                           `duration` BIGINT COMMENT '耗时(毫秒)',
                           `create_time` DATETIME NOT NULL COMMENT '任务到达时间',
                           `delete_time` DATETIME COMMENT '删除时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='待办事项表';

-- ==========================================================
-- 4. 挪动数据 (数据迁移)
-- ==========================================================
INSERT INTO `qf_todo` (
    `id`, `root_id`, `dept_id`, `eng_task_id`, `eng_proc_id`,
    `biz_form_id`, `table_name`, `data_id`, `node_name`, `summary`,
    `member_type`, `member_id`, `initiator_id`, `initiator_name`, `initiator_time`,
    `status`, `fin_member_id`, `fin_member_name`, `fin_time`, `action`,
    `comment`, `duration`, `create_time`, `delete_time`
)
SELECT
    `id`, `root_id`, `dept_id`, `eng_task_id`, `eng_proc_id`,
    `biz_form_id`, `table_name`, `data_id`, `node_name`, `summary`,
    `member_type`, `member_id`, `initiator_id`, `initiator_name`, `initiator_time`,
    `status`, `fin_member_id`, `fin_member_name`, `fin_time`, `action`,
    `comment`, `duration`, `create_time`, `delete_time`
FROM `zremovred_qf_todo`;

-- ==========================================================
-- 改动项说明：
-- 1. 字段扩容：eng_task_id 长度从 VARCHAR(50) 增加到 VARCHAR(200)。
-- 2. 字段扩容：eng_proc_id 长度从 VARCHAR(50) 增加到 VARCHAR(200)。
-- 3. 字段类型变更：action 字段由 VARCHAR(80) 变更为 TINYINT，用于存储操作枚举（0:同意, 1:驳回）。
-- 4. 枚举值增加：status 字段注释增加了“10:已作废”状态说明。
-- 5. 结构规范：统一了字段的默认 NULL 属性，并清理了旧表冗余的字符集声明。
-- ==========================================================