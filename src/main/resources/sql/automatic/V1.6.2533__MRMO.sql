DROP TABLE IF EXISTS core_root;
CREATE TABLE core_root(
                          `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                          `name` VARCHAR(40) NOT NULL  COMMENT '租户名称' ,
                          `expire_time` DATETIME   COMMENT '到期时间(null长期)' ,
                          `remark` VARCHAR(200)   COMMENT '备注' ,
                          `status` TINYINT NOT NULL  COMMENT '状态 1:正常，0:停用' ,
                          `admin_user_id` BIGINT NOT NULL  COMMENT '租户管理账号UID' ,
                          `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                          `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                          `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                          `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                          `delete_time` DATETIME   COMMENT '删除时间' ,
                          PRIMARY KEY (id)
)  COMMENT = '租户表';

DROP TABLE IF EXISTS auth_user_session;
CREATE TABLE auth_user_session(
                                  `id` BIGINT NOT NULL  COMMENT '会话ID' ,
                                  `session_id` VARCHAR(200) NOT NULL  COMMENT '用户凭据' ,
                                  `user_id` BIGINT NOT NULL  COMMENT '用户ID' ,
                                  `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                                  `org_id` BIGINT   COMMENT '直属企业ID' ,
                                  `dept_id` BIGINT   COMMENT '直属部门ID' ,
                                  `root_name` VARCHAR(40) NOT NULL  COMMENT '租户名' ,
                                  `org_name` VARCHAR(80)   COMMENT '直属企业名' ,
                                  `dept_name` VARCHAR(80)   COMMENT '直属部门名' ,
                                  `username` VARCHAR(80) NOT NULL  COMMENT '用户名' ,
                                  `nickname` VARCHAR(80)   COMMENT '用户昵称' ,
                                  `permissions` JSON NOT NULL  COMMENT '用户权限代码JSON' ,
                                  `rs_max` TINYINT NOT NULL  COMMENT '最大RS等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织' ,
                                  `rs_allow_org_ids` JSON NOT NULL  COMMENT 'RowScope允许访问的组织IDS' ,
                                  `data_version` BIGINT NOT NULL  COMMENT '数据版本号(应与用户表对齐)' ,
                                  `expires_at` DATETIME NOT NULL  COMMENT '会话过期时间' ,
                                  `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                                  `creator_id` BIGINT NOT NULL  COMMENT '创建者ID(用于兼容RS数据权限 必须与UserId一致)' ,
                                  `update_time` DATETIME NOT NULL  COMMENT '修改时间' ,
                                  PRIMARY KEY (id)
)  COMMENT = '用户会话表';

CREATE UNIQUE INDEX uk_session_id ON auth_user_session(session_id);

-- 1. 删除可能存在的 zremovred_ 备份表
DROP TABLE IF EXISTS `zremovred_core_user`;

-- 2. 重命名旧表，增加 zremovred_ 前缀
-- 注意：确保此时没有业务正在写入 core_user
RENAME TABLE `core_user` TO `zremovred_core_user`;

-- 3. 创建新表
CREATE TABLE `core_user` (
                             `id` BIGINT NOT NULL COMMENT '用户ID',
                             `root_id` BIGINT NOT NULL COMMENT '租户ID',
                             `org_id` BIGINT COMMENT '直属企业ID',
                             `dept_id` BIGINT COMMENT '直属部门ID',
                             `username` VARCHAR(80) NOT NULL COMMENT '用户名',
                             `password` VARCHAR(1280) NOT NULL COMMENT '密码',
                             `nickname` VARCHAR(80) COMMENT '昵称',
                             `gender` TINYINT COMMENT '性别 0:男 1:女 2:不愿透露',
                             `phone` VARCHAR(20) COMMENT '手机号码',
                             `email` VARCHAR(128) COMMENT '邮箱',
                             `login_count` INT NOT NULL COMMENT '登录次数',
                             `status` TINYINT NOT NULL COMMENT '用户状态 0:正常 1:封禁',
                             `last_login_time` DATETIME COMMENT '最后登录时间',
                             `avatar_attach_id` BIGINT COMMENT '用户头像附件ID',
                             `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '内置用户 0:否 1:是',
                             `data_version` BIGINT NOT NULL COMMENT '数据版本号',
                             `create_time` DATETIME NOT NULL COMMENT '创建时间',
                             `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                             `update_time` DATETIME NOT NULL COMMENT '修改时间',
                             `updater_id` BIGINT NOT NULL COMMENT '更新人ID',
                             `delete_time` DATETIME COMMENT '删除时间 为NULL未删',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';

-- 4. 挪动数据
-- 映射说明：
-- root_id -> root_id
-- org_id  <- 默认初始化为 NULL (旧表无此明确字段，或根据逻辑映射)
-- dept_id -> dept_id
INSERT INTO `core_user` (
    `id`, `root_id`, `org_id`, `dept_id`, `username`, `password`,
    `nickname`, `gender`, `phone`, `email`, `login_count`, `status`,
    `last_login_time`, `avatar_attach_id`, `is_system`, `data_version`,
    `create_time`, `creator_id`, `update_time`, `updater_id`, `delete_time`
)
SELECT
    `id`, `root_id`, NULL, `dept_id`, `username`, `password`,
    `nickname`, `gender`, `phone`, `email`, `login_count`, `status`,
    `last_login_time`, `avatar_attach_id`, `is_system`, `data_version`,
    `create_time`, `creator_id`, `update_time`, `updater_id`, `delete_time`
FROM `zremovred_core_user`;

/*
============================================================
改动项说明 (Notes of Changes):
1. 结构变更：
   - 新增字段 `org_id` (直属企业ID)，迁移过程中暂初始化为 NULL。
   - 移除旧表字段 `root_name` (所属企业名称)，新表不再维护冗余名称。
   - 移除旧表字段 `dept_name` (部门名称)，新表不再维护冗余名称。
   - 移除兼容字段 `active_company_id` 和 `active_env_id`。

2. 属性变更：
   - `root_id` 的注释从 "所属企业ID" 变更为 "租户ID"。
   - `dept_id` 的注释从 "部门ID" 变更为 "直属部门ID"。
   - 移除了字段定义中显式的 `CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci`
     (已在表级别定义，简化了脚本阅读性)。

3. 约束变更：
   - 主键 `PRIMARY KEY (id)` 保持不变，去除了显式的 `USING BTREE` 关键字（MySQL默认即为BTREE）。
============================================================
*/


-- =============================================================================
-- 1. 删除可能存在的旧备份表 zremovred_core_org
-- =============================================================================
DROP TABLE IF EXISTS `zremovred_core_org`;

-- =============================================================================
-- 2. 重命名当前旧表，增加 zremovred_ 前缀
-- =============================================================================
RENAME TABLE `core_org` TO `zremovred_core_org`;

-- =============================================================================
-- 3. 创建新表 core_org
-- =============================================================================
CREATE TABLE `core_org` (
                            `id` BIGINT NOT NULL COMMENT '主键id',
                            `root_id` BIGINT NOT NULL COMMENT '租户ID',
                            `top_id` BIGINT NOT NULL COMMENT '顶级企业ID',
                            `org_id` BIGINT NOT NULL COMMENT '直属企业ID',
                            `parent_id` BIGINT DEFAULT NULL COMMENT '上级组织ID NULL为顶级',
                            `org_path_ids` VARCHAR(2000) DEFAULT NULL COMMENT '从顶级组织到当前组织的ID列表 以,分割',
                            `kind` TINYINT NOT NULL COMMENT '类型 0:企业 1:子企业 2:部门',
                            `name` VARCHAR(80) NOT NULL COMMENT '组织机构名称',
                            `short_name` VARCHAR(40) DEFAULT NULL COMMENT '组织机构简称',
                            `level` TINYINT NOT NULL COMMENT '组织机构级别',
                            `principal_id` BIGINT DEFAULT NULL COMMENT '主管ID',
                            `seq` INT NOT NULL DEFAULT 0 COMMENT '排序',
                            `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
                            `create_time` DATETIME NOT NULL COMMENT '创建时间',
                            `creator_id` BIGINT NOT NULL COMMENT '创建人id',
                            `update_time` DATETIME NOT NULL COMMENT '更新时间',
                            `updater_id` BIGINT NOT NULL COMMENT '更新人id',
                            `delete_time` DATETIME DEFAULT NULL COMMENT '删除时间 NULL未删除',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT = '组织机构-核心';

-- =============================================================================
-- 4. 挪动数据（从 zremovred_core_org 迁移至 core_org）
-- 注：针对新表中的 NOT NULL 字段进行了默认值逻辑处理
-- =============================================================================
INSERT INTO `core_org` (
    `id`,
    `root_id`,
    `top_id`,
    `org_id`,
    `parent_id`,
    `org_path_ids`,
    `kind`,
    `name`,
    `level`,
    `principal_id`,
    `seq`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
)
SELECT
    `id`,
    `root_id`,
    `root_id`,               -- top_id: 逻辑映射，旧表无此字段，暂同步 root_id
    `root_id`,               -- org_id: 逻辑映射，旧表无此字段，暂同步 root_id
    `parent_id`,
    `org_path_ids`,
    `kind`,
    `name`,                  -- 注意：新表长度由128缩减至80，若超长会截断
    1,                       -- level: 新表必填，旧表无此字段，默认初始化为1
    `principal_id`,
    `seq`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
FROM `zremovred_core_org`;

/*
=============================================================================
改动项注释说明：

1. 字段新增：
   - [top_id]: 新增必填字段“顶级企业ID”，初始化数据时暂从 root_id 映射。
   - [org_id]: 新增必填字段“直属企业ID”，初始化数据时暂从 root_id 映射。
   - [name_short]: 新增字段“组织机构简称”。
   - [level]: 新增必填字段“组织机构级别”，初始化数据时统一设为 1。
   - [remark]: 新增字段“备注”。

2. 字段删除：
   - [principal_name]: 新表删除了“主管名称”字段，仅保留 ID。

3. 字段变更：
   - [root_id]: 注释由“一级组织ID”变更为“租户ID”。
   - [org_path_ids]: 长度从 VARCHAR(1024) 扩展至 VARCHAR(2000)。
   - [name]: 长度从 VARCHAR(128) 缩减至 VARCHAR(80)，迁移时需注意超长数据。
   - [kind]: 注释类型减少了“3:班组”，仅保留 0, 1, 2。
   - [parent_id]: 移除 CHARACTER SET 指定（由表级别 utf8mb4 继承）。

4. 结构调整：
   - 移除 ROW_FORMAT=DYNAMIC（依赖系统默认值）。
   - 移除了 PRIMARY KEY 处的 USING BTREE 显式指定（MySQL 8.0 默认即为 BTREE）。
=============================================================================
*/

-- =================================================================
-- 1. 删除 zremovred_旧表 (如果已存在)
-- =================================================================
DROP TABLE IF EXISTS `zremovred_auth_group`;

-- =================================================================
-- 2. 重命名旧表 增加zremovred_前缀
-- =================================================================
RENAME TABLE `auth_group` TO `zremovred_auth_group`;

-- =================================================================
-- 3. 创建新表
-- =================================================================
CREATE TABLE `auth_group` (
                              `id` BIGINT NOT NULL COMMENT '组ID',
                              `root_id` BIGINT NOT NULL COMMENT '租户ID',
                              `org_id` BIGINT COMMENT '直属企业ID 为NULL代表租户角色',
                              `code` VARCHAR(32) NOT NULL COMMENT '组标识，如：admin、developer等',
                              `name` VARCHAR(80) NOT NULL COMMENT '组名称，如：管理员组、开发者组等',
                              `remark` VARCHAR(200) COMMENT '组描述',
                              `status` TINYINT NOT NULL COMMENT '组状态:0:禁用，1:启用',
                              `seq` INT NOT NULL COMMENT '排序号',
                              `row_scope` TINYINT NOT NULL COMMENT 'RS等级 0:全集团 10:本公司+下级公司 20:仅本公司 30:本部门+下级部门 40:仅本部门 50:仅本人 60:指定组织',
                              `is_system` TINYINT NOT NULL COMMENT '系统内置组 0:否 1:是',
                              `create_time` DATETIME NOT NULL COMMENT '创建时间',
                              `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                              `update_time` DATETIME NOT NULL COMMENT '修改时间',
                              `updater_id` BIGINT NOT NULL COMMENT '修改人ID',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT = '用户组';

-- =================================================================
-- 4. 挪动数据
-- 注意：由于新表 root_id 为 NOT NULL，此处默认赋值为 0，请根据实际业务逻辑调整
-- =================================================================
INSERT INTO `auth_group` (
    `id`,
    `root_id`,
    `org_id`,
    `code`,
    `name`,
    `remark`,
    `status`,
    `seq`,
    `row_scope`,
    `is_system`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`
)
SELECT
    `id`,
    0,            -- root_id 默认值补齐
    NULL,         -- org_id 默认值补齐
    LEFT(`code`, 32), -- 截断处理，防止旧数据长度超过新表限制
    `name`,
    LEFT(`remark`, 200), -- 截断处理，从 TEXT 转为 VARCHAR(200)
    `status`,
    `seq`,
    `row_scope`,
    `is_system`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`
FROM `zremovred_auth_group`;

-- =================================================================
-- 注释说明：改动项汇总
-- =================================================================
/*
改动项说明：
1. 字段新增：
   - 新增 `root_id` (BIGINT, NOT NULL)，用于多租户架构。
   - 新增 `org_id` (BIGINT, NULLable)，用于区分租户角色与企业角色。

2. 字段类型与长度变更：
   - `code`: 长度由 VARCHAR(80) 缩减至 VARCHAR(32)。
   - `remark`: 类型由 TEXT 更改为 VARCHAR(200)，存储空间更紧凑。

3. 索引变更：
   - 移除了旧表的唯一索引 `uk_code` (需确认业务是否不再需要该唯一约束)。
   - 移除了旧表主键后的 `USING BTREE` 显示声明（MySQL 8.0 默认即为 BTREE）。

4. 默认值调整：
   - `row_scope`: 移除了旧表中的 DEFAULT '0' 约束，改为强制手动插入。

5. 存储特性：
   - 移除了 `ROW_FORMAT=DYNAMIC` 的显示指定（MySQL 8.0 会根据配置自动选择最优格式）。
*/