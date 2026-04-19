-- =============================================================================
-- 1. 删除可能存在的旧备份表
-- =============================================================================
DROP TABLE IF EXISTS `zremovred_qf_todo`;

-- =============================================================================
-- 2. 重命名当前旧表为备份表
-- =============================================================================
RENAME TABLE `qf_todo` TO `zremovred_qf_todo`;

-- =============================================================================
-- 3. 创建新表
-- =============================================================================
CREATE TABLE `qf_todo` (
                           `id` BIGINT NOT NULL COMMENT '主键ID',
                           `root_id` BIGINT NOT NULL COMMENT '租户ID',
                           `dept_id` BIGINT NOT NULL COMMENT '部门ID',
                           `eng_task_id` VARCHAR(50) NOT NULL COMMENT '引擎任务ID',
                           `eng_proc_id` VARCHAR(50) NOT NULL COMMENT '引擎流程ID',
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
                           `action` VARCHAR(80) COMMENT '操作',
                           `comment` VARCHAR(500) COMMENT '办理人意见',
                           `duration` BIGINT COMMENT '耗时(毫秒)',
                           `create_time` DATETIME NOT NULL COMMENT '任务到达时间',
                           `delete_time` DATETIME COMMENT '删除时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='待办事项表';

-- =============================================================================
-- 4. 挪动数据 (数据迁移)
-- 逻辑：将旧表数据导入新表，新增字段 status 默认初始化为 0 (待办)
-- =============================================================================
INSERT INTO `qf_todo` (
    `id`, `root_id`, `dept_id`, `eng_task_id`, `eng_proc_id`,
    `biz_form_id`, `table_name`, `data_id`, `node_name`, `summary`,
    `member_type`, `member_id`, `initiator_id`, `initiator_name`,
    `initiator_time`, `status`, `create_time`, `delete_time`
)
SELECT
    `id`, `root_id`, `dept_id`, `eng_task_id`, `eng_proc_id`,
    `biz_form_id`, `table_name`, `data_id`, `node_name`, `summary`,
    `member_type`, `member_id`, `initiator_id`, `initiator_name`,
    `initiator_time`, 0, `create_time`, `delete_time`
FROM `zremovred_qf_todo`;

-- =============================================================================
-- 修改项注释说明：
-- 1. 新增字段 [status]: 标记待办/已办状态，迁移数据默认值为 0。
-- 2. 新增字段 [fin_member_id]: 记录实际办理人 ID。
-- 3. 新增字段 [fin_member_name]: 记录实际办理人姓名。
-- 4. 新增字段 [fin_time]: 记录任务实际处理完成的时间。
-- 5. 新增字段 [action]: 记录办理动作（如：同意、驳回）。
-- 6. 新增字段 [comment]: 记录办理人的审批意见。
-- 7. 新增字段 [duration]: 记录从任务到达至办理完成的耗时。
-- 8. 规范化调整: 统一移除了旧表中部分字段显式声明的 COLLATE，采用表级统一字符集。
-- =============================================================================