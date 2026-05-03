ALTER TABLE `core_user` MODIFY COLUMN `status` tinyint NOT NULL COMMENT '用户状态 0:封禁 1:正常' AFTER `login_count`;

-- =================================================================================
-- 1. 删除 zremovred_旧表 (清理之前的备份，防止命名冲突)
-- =================================================================================
DROP TABLE IF EXISTS `zremovred_core_menu`;

-- =================================================================================
-- 2. 重命名当前旧表，增加 zremovred_ 前缀作为备份
-- =================================================================================
ALTER TABLE `core_menu` RENAME TO `zremovred_core_menu`;

-- =================================================================================
-- 3. 创建新表 core_menu
-- =================================================================================
CREATE TABLE `core_menu` (
                             `id` BIGINT NOT NULL COMMENT '主键ID',
                             `root_id` BIGINT NOT NULL COMMENT '租户ID',
                             `parent_id` BIGINT COMMENT '父级项ID',
                             `name` VARCHAR(40) NOT NULL COMMENT '菜单项名',
                             `kind` TINYINT NOT NULL COMMENT '菜单项类型 0:目录 1:菜单 2:按钮',
                             `path` VARCHAR(512) COMMENT '指向路径',
                             `icon` VARCHAR(80) COMMENT '菜单图标',
                             `hide` TINYINT NOT NULL COMMENT '隐藏 0:否 1:是',
                             `permission_code` JSON NOT NULL COMMENT '所需权限码Set<String>',
                             `seq` INT NOT NULL COMMENT '排序',
                             `remark` VARCHAR(200) COMMENT '备注',
                             `create_time` DATETIME NOT NULL COMMENT '创建时间',
                             `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                             `update_time` DATETIME NOT NULL COMMENT '更新时间',
                             `updater_id` BIGINT NOT NULL COMMENT '更新人ID',
                             `delete_time` DATETIME COMMENT '删除时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '菜单表';

-- =================================================================================
-- 4. 挪动数据 (数据转换迁移)
-- =================================================================================
INSERT INTO `core_menu` (
    `id`, `root_id`, `parent_id`, `name`, `kind`, `path`, `icon`, `hide`,
    `permission_code`, `seq`, `remark`, `create_time`, `creator_id`,
    `update_time`, `updater_id`, `delete_time`
)
SELECT
    `id`,
    `root_id`,
    `parent_id`,
    `name`,
    `kind`,
    `path`,
    `icon`,
    `hide`,
    -- 处理 permission_code: 将逗号分隔的字符串转换为 JSON 数组
    -- 使用 JSON_ARRAY() 处理空值，使用 JSON_OVERLAPS 处理转换逻辑（此处采用最通用的字符串转数组逻辑）
    CASE
        WHEN `permission_code` IS NULL OR `permission_code` = '' THEN '[]'
        ELSE CAST(CONCAT('["', REPLACE(`permission_code`, ',', '","'), '"]') AS JSON)
        END,
    `seq`,
    -- 备注字段长度缩减，使用 LEFT 函数防止溢出
    LEFT(`remark`, 200),
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
FROM `zremovred_core_menu`;

-- =================================================================================
-- 注释说明：改动项汇总
-- =================================================================================
/*
改动项说明：
1. 移除字段：
   - 移除了 `dept_id` (部门ID) 字段，新结构不再包含此维度。

2. 字段长度调整：
   - `name`: 从 VARCHAR(32) 扩展至 VARCHAR(40)。
   - `path`: 从 VARCHAR(500) 扩展至 VARCHAR(512)。
   - `remark`: 从 TEXT (65535字符) 缩减至 VARCHAR(200)。迁移脚本已加入 LEFT() 截断保护。

3. 数据类型变更：
   - `permission_code`: 从 VARCHAR(500) 变更为 JSON 类型。
   - 迁移逻辑：自动将旧版的 "sys:user:add,sys:user:edit" 格式转换为 JSON 数组格式 ["sys:user:add", "sys:user:edit"]。

4. 约束与默认值：
   - 显式指定了 ENGINE=InnoDB 与字符集，确保与 MySQL 8.0 最佳实践一致。
   - 修正了旧表中可能存在的 NULL/NOT NULL 逻辑对齐。
*/