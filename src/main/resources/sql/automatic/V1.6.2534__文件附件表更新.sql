-- =================================================================================
-- 1. 删除可能存在的旧备份表（预防性清理）
-- =================================================================================
DROP TABLE IF EXISTS `zremovred_core_attach`;

-- =================================================================================
-- 2. 重命名当前旧表，增加 zremovred_ 前缀
-- =================================================================================
RENAME TABLE `core_attach` TO `zremovred_core_attach`;

-- =================================================================================
-- 3. 创建新表 core_attach
-- =================================================================================
CREATE TABLE `core_attach` (
                               `id` BIGINT NOT NULL COMMENT '文件ID',
                               `root_id` BIGINT NOT NULL COMMENT '租户ID',
                               `name` VARCHAR(80) NOT NULL COMMENT '文件原始名称',
                               `kind` VARCHAR(32) NOT NULL COMMENT '文件业务类型',
                               `suffix` VARCHAR(80) COMMENT '扩展名',
                               `path` VARCHAR(2048) NOT NULL COMMENT '文件路径',
                               `sha256` VARCHAR(64) NOT NULL COMMENT '文件摘要SHA256',
                               `total_size` BIGINT NOT NULL COMMENT '总大小',
                               `receive_size` BIGINT NOT NULL COMMENT '已接收大小',
                               `status` TINYINT NOT NULL COMMENT '状态 0:预检文件 1:区块不完整 2:校验中 3:有效',
                               `verify_time` DATETIME COMMENT '校验时间',
                               `create_time` DATETIME NOT NULL COMMENT '创建时间',
                               `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='文件附件表';

-- =================================================================================
-- 4. 挪动数据（从备份表迁移至新表）
-- =================================================================================
INSERT INTO `core_attach` (
    `id`,
    `root_id`,
    `name`,
    `kind`,
    `suffix`,
    `path`,
    `sha256`,
    `total_size`,
    `receive_size`,
    `status`,
    `verify_time`,
    `create_time`,
    `creator_id`
)
SELECT
    `id`,
    `root_id`,
    SUBSTRING(`name`, 1, 80),   -- 截断处理以匹配新长度
    SUBSTRING(`kind`, 1, 32),   -- 截断处理以匹配新长度
    SUBSTRING(`suffix`, 1, 80), -- 截断处理以匹配新长度
    `path`,
    SUBSTRING(`sha256`, 1, 64), -- 截断处理以匹配新长度
    `total_size`,
    `receive_size`,
    `status`,
    `verify_time`,
    `create_time`,
    `creator_id`
FROM `zremovred_core_attach`;

-- =================================================================================
-- 迁移说明及改动项注释
-- =================================================================================
/*
【改动项说明】：
1. 字段删除：
   - 删除了 `dept_id` (部门ID) 字段，新表不再维护此维度。
2. 字段长度缩减（请注意数据截断风险）：
   - `name`: 从 VARCHAR(128) 缩减至 VARCHAR(80)
   - `kind`: 从 VARCHAR(64) 缩减至 VARCHAR(32)
   - `suffix`: 从 VARCHAR(128) 缩减至 VARCHAR(80)
   - `sha256`: 从 VARCHAR(320) 大幅缩减至 VARCHAR(64)
3. 字段长度增加：
   - `path`: 从 VARCHAR(256) 扩容至 VARCHAR(2048)，以支持更深的存储路径。
4. 状态值定义变更：
   - `status`: 注释增加了 "2:校验中"，并将原有的 "2:有效" 顺延或重新定义为 "3:有效"。
5. 字段注释完善：
   - `sha256` 注释修正为 "文件摘要SHA256"。
   - `creator_id` 注释修正为 "创建人ID"。
6. 结构优化：
   - 明确为新表添加了 `PRIMARY KEY` 约束，保持与旧表一致的查询性能。
*/