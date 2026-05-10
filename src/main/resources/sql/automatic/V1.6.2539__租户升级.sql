-- 1. 如果存在之前的备份表，则先删除（确保重命名操作不冲突）
DROP TABLE IF EXISTS `zremovred_core_root`;

-- 2. 将当前运行的旧表重命名，增加 zremovred_ 前缀进行备份
RENAME TABLE `core_root` TO `zremovred_core_root`;

-- 3. 创建新结构的 core_root 表
CREATE TABLE `core_root` (
                             `id` BIGINT NOT NULL COMMENT '主键ID',
                             `name` VARCHAR(40) NOT NULL COMMENT '租户名称',
                             `expire_time` DATETIME COMMENT '到期时间(null长期)',
                             `remark` VARCHAR(200) COMMENT '备注',
                             `status` TINYINT NOT NULL COMMENT '状态 0:禁用 1:正常',
                             `admin_user_id` BIGINT NOT NULL COMMENT '租户管理账号UID',
                             `admin_group_id` BIGINT NOT NULL COMMENT '租户管理角色GID',
                             `is_system` TINYINT NOT NULL COMMENT '内置租户 0:否 1:是',
                             `create_time` DATETIME NOT NULL COMMENT '创建时间',
                             `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                             `update_time` DATETIME NOT NULL COMMENT '更新时间',
                             `updater_id` BIGINT NOT NULL COMMENT '更新人ID',
                             `delete_time` DATETIME COMMENT '删除时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '租户表';

-- 4. 从备份表中挪动数据，并初始化新字段 admin_group_id 为 -1
INSERT INTO `core_root` (
    `id`,
    `name`,
    `expire_time`,
    `remark`,
    `status`,
    `admin_user_id`,
    `admin_group_id`,
    `is_system`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
)
SELECT
    `id`,
    `name`,
    `expire_time`,
    `remark`,
    `status`,
    `admin_user_id`,
    -1, -- 初始化新的 group_id 为 -1
    `is_system`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
FROM `zremovred_core_root`;

/*
============================================================
迁移改动项说明：
1. 【字段新增】：新增了 `admin_group_id` 字段（BIGINT 类型），用于存储租户管理角色GID。
2. 【数据初始化】：迁移过程中，所有存量数据的 `admin_group_id` 字段统一初始化为 -1。
3. 【表结构变更】：
    - 删除了原字段的 `COLLATE utf8mb4_general_ci` 显式声明（新表统一继承表级别的排序规则）。
    - 移除了 `is_system` 字段的默认值 '0'（根据提供的新表 DDL 确定）。
4. 【安全性操作】：采用 RENAME 方式备份原表为 `zremovred_core_root`，确保迁移期间数据可回滚。
============================================================
*/