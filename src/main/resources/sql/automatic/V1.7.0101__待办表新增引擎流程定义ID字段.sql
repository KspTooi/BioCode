-- 在 qf_todo 表的 biz_form_id 字段后新增 eng_process_def_id 字段
ALTER TABLE qf_todo 
ADD COLUMN eng_process_def_id VARCHAR(200) COMMENT '引擎流程ID(部署失败为NULL)' 
AFTER biz_form_id;
