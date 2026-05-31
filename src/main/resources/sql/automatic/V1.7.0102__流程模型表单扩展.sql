ALTER TABLE qf_model
    ADD COLUMN form_id BIGINT COMMENT '关联表单ID' AFTER group_id;

ALTER TABLE qf_model_deploy_rcd
    ADD COLUMN form_id BIGINT DEFAULT 0 COMMENT '关联表单ID' AFTER model_id;

-- 然后更新现有数据，设置合理的form_id值
UPDATE qf_model_deploy_rcd SET form_id = -1 WHERE form_id IS NULL;

-- 最后修改为NOT NULL
ALTER TABLE qf_model_deploy_rcd
    MODIFY COLUMN form_id BIGINT NOT NULL COMMENT '关联表单ID';