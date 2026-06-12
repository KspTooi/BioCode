SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- 1. auth_basic_pat 基本PAT表（幂等：CREATE TABLE IF NOT EXISTS）
-- =================================================================================
CREATE TABLE IF NOT EXISTS `auth_basic_pat` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `root_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `name` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'PAT名称',
  `pat_pt` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '部分明文',
  `pat_ct` varchar(2048) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密文',
  `expire` datetime DEFAULT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL COMMENT '状态: 0:禁用 1:启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `updater_id` bigint NOT NULL COMMENT '更新人ID',
  `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='基本PAT';

-- =================================================================================
-- 2. 在 用户与组织 下添加 基本PAT 菜单（幂等：先检查再插入）
-- =================================================================================
INSERT INTO `core_menu` (
    `id`, `root_id`, `parent_id`, `name`, `kind`, `path`, `icon`, `hide`,
    `permission_code`, `seq`, `remark`, `create_time`, `creator_id`,
    `update_time`, `updater_id`
)
SELECT
    1350000000000000010,
    -1,
    (SELECT `id` FROM `core_menu` WHERE `name` = '用户与组织' AND `parent_id` IS NULL AND `delete_time` IS NULL LIMIT 1),
    '基本PAT',
    1,
    '/auth/basic-pat',
    'Key',
    0,
    JSON_ARRAY('auth:basic:pat:view', 'auth:basic:pat:add', 'auth:basic:pat:edit', 'auth:basic:pat:remove'),
    5,
    '基本PAT管理',
    NOW(),
    0,
    NOW(),
    0
FROM (SELECT 1) AS dummy
WHERE NOT EXISTS (
    SELECT 1 FROM `core_menu`
    WHERE `name` = '基本PAT'
      AND `parent_id` = (SELECT `id` FROM `core_menu` WHERE `name` = '用户与组织' AND `parent_id` IS NULL AND `delete_time` IS NULL LIMIT 1)
      AND `delete_time` IS NULL
);

SET FOREIGN_KEY_CHECKS = 1;
