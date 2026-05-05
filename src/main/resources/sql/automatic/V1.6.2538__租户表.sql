-- ========================================================
-- 1. 删除可能存在的旧备份表 (zremovred_core_root)
-- ========================================================
DROP TABLE IF EXISTS `zremovred_core_root`;

-- ========================================================
-- 2. 重命名当前旧表，增加 zremovred_ 前缀
-- ========================================================
RENAME TABLE `core_root` TO `zremovred_core_root`;

-- ========================================================
-- 3. 创建新表 core_root
-- ========================================================
CREATE TABLE `core_root` (
                             `id` BIGINT NOT NULL COMMENT '主键ID',
                             `name` VARCHAR(40) NOT NULL COMMENT '租户名称',
                             `expire_time` DATETIME COMMENT '到期时间(null长期)',
                             `remark` VARCHAR(200) COMMENT '备注',
                             `status` TINYINT NOT NULL COMMENT '状态 0:禁用 1:正常',
                             `admin_user_id` BIGINT NOT NULL COMMENT '租户管理账号UID',
                             `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '内置租户 0:否 1:是',
                             `create_time` DATETIME NOT NULL COMMENT '创建时间',
                             `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                             `update_time` DATETIME NOT NULL COMMENT '更新时间',
                             `updater_id` BIGINT NOT NULL COMMENT '更新人ID',
                             `delete_time` DATETIME COMMENT '删除时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '租户表';

-- ========================================================
-- 4. 挪动数据 (从备份表导入新表)
-- ========================================================
INSERT INTO `core_root` (
    `id`,
    `name`,
    `expire_time`,
    `remark`,
    `status`,
    `admin_user_id`,
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
    0, -- 为新字段 is_system 提供默认值 0
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
FROM `zremovred_core_root`;

-- ========================================================
-- 注释说明 (改动项):
-- 1. 结构变更：新增了字段 `is_system` (TINYINT)，用于标识是否为内置租户。
-- 2. 数据处理：在迁移过程中，旧有数据行的 `is_system` 字段统一初始化为 0 (否)。
-- 3. 约束调整：移除了旧表字段中冗余的 COLLATE 定义（由表级别定义承接），保持脚本整洁。
-- 4. 默认值：在新表定义中为 `is_system` 显式增加了 `DEFAULT 0` 以增强安全性。
-- ========================================================