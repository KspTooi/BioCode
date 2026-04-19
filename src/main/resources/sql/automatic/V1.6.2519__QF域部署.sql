-- ==========================================================
-- 1. 删除旧的备份表 (如果存在)
-- ==========================================================
DROP TABLE IF EXISTS `zremovred_qf_model_deploy_rcd`;

-- ==========================================================
-- 2. 重命名当前表为备份表
-- ==========================================================
ALTER TABLE `qf_model_deploy_rcd` RENAME TO `zremovred_qf_model_deploy_rcd`;

-- ==========================================================
-- 3. 创建新表
-- ==========================================================
CREATE TABLE `qf_model_deploy_rcd` (
                                       `id` BIGINT NOT NULL COMMENT '主键ID',
                                       `root_id` BIGINT NOT NULL COMMENT '所属企业/租户ID',
                                       `dept_id` BIGINT NOT NULL COMMENT '所属部门ID',
                                       `model_id` BIGINT NOT NULL COMMENT '模型ID',
                                       `name` VARCHAR(80) NOT NULL COMMENT '模型名称',
                                       `code` VARCHAR(32) NOT NULL COMMENT '模型编码',
                                       `bpmn_xml` LONGTEXT COMMENT '模型BPMN XML',
                                       `version` INT NOT NULL COMMENT '模型版本号',
                                       `eng_deployment_id` VARCHAR(200) DEFAULT NULL COMMENT '引擎部署ID(部署失败为NULL)',
                                       `eng_process_def_id` VARCHAR(200) DEFAULT NULL COMMENT '引擎流程ID(部署失败为NULL)',
                                       `eng_deploy_result` VARCHAR(2000) NOT NULL COMMENT '引擎返回的部署结果',
                                       `status` TINYINT NOT NULL COMMENT '部署状态 0:正常 1:部署失败',
                                       `create_time` DATETIME NOT NULL COMMENT '创建时间',
                                       `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
                                       `update_time` DATETIME NOT NULL COMMENT '更新时间',
                                       `updater_id` BIGINT NOT NULL COMMENT '更新人ID',
                                       `delete_time` DATETIME DEFAULT NULL COMMENT '删除时间 NULL未删',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='流程模型部署历史';

-- ==========================================================
-- 4. 挪动数据 (数据迁移)
-- ==========================================================
INSERT INTO `qf_model_deploy_rcd` (
    `id`,
    `root_id`,
    `dept_id`,
    `model_id`,
    `name`,
    `code`,
    `bpmn_xml`,
    `version`,
    `eng_deployment_id`,
    `eng_process_def_id`,
    `eng_deploy_result`,
    `status`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
)
SELECT
    `id`,
    `root_id`,
    `dept_id`,
    `model_id`,
    `name`,
    `code`,
    `bpmn_xml`,
    `version`,
    `eng_deployment_id`,
    `eng_process_def_id`,
    `eng_deploy_result`,
    `status`,
    `create_time`,
    `creator_id`,
    `update_time`,
    `updater_id`,
    `delete_time`
FROM `zremovred_qf_model_deploy_rcd`;

-- ==========================================================
-- 改动项说明：
-- 1. 字段长度变更：eng_deployment_id 字段长度由 VARCHAR(50) 扩容至 VARCHAR(200)。
-- 2. 字段长度变更：eng_process_def_id 字段长度由 VARCHAR(50) 扩容至 VARCHAR(200)。
-- 3. 结构优化：保留了 utf8mb4_general_ci 校对集，确保与旧数据兼容。
-- 4. 备份策略：旧表已重命名为 zremovred_qf_model_deploy_rcd 以供回滚参考。
-- ==========================================================