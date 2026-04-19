-- 1. 删除 zremovred_旧表 (如果存在)
DROP TABLE IF EXISTS `zremovred_qf_todo`;

-- 2. 重命名旧表，增加 zremovred_ 前缀
RENAME TABLE `qf_todo` TO `zremovred_qf_todo`;

-- 3. 创建新表
CREATE TABLE `qf_todo` (
                           `id` bigint NOT NULL COMMENT '主键ID',
                           `root_id` bigint NOT NULL COMMENT '租户ID',
                           `dept_id` bigint NOT NULL COMMENT '部门ID',
                           `eng_task_id` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '引擎任务ID',
                           `eng_proc_id` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '引擎流程ID',
                           `biz_form_id` bigint NOT NULL COMMENT '业务表单ID',
                           `table_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物理表名(带入业务表单数据)',
                           `data_id` bigint NOT NULL COMMENT '物理表数据主键ID',
                           `node_name` varchar(80) COLLATE utf8mb4_general_ci NOT NULL COMMENT '当前节点名称 (如: 财务总监审批)',
                           `summary` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '摘要(如：张三提交的 5000 元报销)',
                           `member_type` tinyint NOT NULL COMMENT '办理成员类型 0:办理人, 1:候选组',
                           `member_id` bigint NOT NULL COMMENT '办理成员ID (用户ID或用户组标识)',
                           `initiator_id` bigint NOT NULL COMMENT '发起人ID',
                           `initiator_name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '发起人名',
                           `initiator_time` datetime NOT NULL COMMENT '发起时间',
                           `status` tinyint NOT NULL COMMENT '待办状态 0:待办 1:已办',
                           `fin_member_id` bigint DEFAULT NULL COMMENT '实际办理人ID',
                           `fin_member_name` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '实际办理人姓名',
                           `fin_time` datetime DEFAULT NULL COMMENT '实际办理时间',
                           `action` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作',
                           `comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '办理人意见',
                           `duration` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
                           `create_time` datetime NOT NULL COMMENT '任务到达时间',
                           `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='待办事项表';

-- 4. 挪动数据
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
    `status`, `fin_member_id`, `fin_member_name`, `fin_time`, CAST(`action` AS CHAR),
    `comment`, `duration`, `create_time`, `delete_time`
FROM `zremovred_qf_todo`;

/*
================================================================
改动项说明：
1. 字符集变更：新表显式指定了 utf8mb4 字符集及 utf8mb4_general_ci 排序规则，增强了对表情符号及多语言的支持。
2. 字段类型调整：`action` 字段由原来的 TINYINT (0:同意 1:驳回) 更改为 VARCHAR(80)。
   - 迁移脚本在插入时使用了 CAST(`action` AS CHAR) 以确保数据平滑过渡。
3. 字段注释调整：
   - `status` 字段的注释移除了 "10:已作废" 的描述。
   - `action` 字段的注释由具体的数字含义简略为 "操作"。
4. 默认值规范化：新表对可为空的字段显式增加了 DEFAULT NULL 声明。
5. 存储引擎确认：新表明确指定使用 InnoDB 引擎。
================================================================
*/