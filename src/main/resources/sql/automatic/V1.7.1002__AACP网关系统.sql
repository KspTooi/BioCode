DROP TABLE IF EXISTS aacp_provider;
CREATE TABLE aacp_provider(
                              `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                              `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                              `name` VARCHAR(80) NOT NULL  COMMENT '供应商名称' ,
                              `code` VARCHAR(32) NOT NULL  COMMENT '供应商代码' ,
                              `api_kind` VARCHAR(255) NOT NULL  COMMENT '接口类型 0:OpenAi 1:Anthropic' ,
                              `api_key` VARCHAR(2000)   COMMENT '接口密钥' ,
                              `api_host` VARCHAR(512) NOT NULL  COMMENT '接口地址' ,
                              `api_url` VARCHAR(512) NOT NULL  COMMENT '接口端点' ,
                              `proxy_kind` TINYINT NOT NULL  COMMENT '代理类型 0:无 1:HTTP 2:SOCKS5' ,
                              `proxy_url` VARCHAR(512)   COMMENT '代理地址' ,
                              `status` TINYINT NOT NULL  COMMENT '状态 0:禁用 1:启用' ,
                              `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                              `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                              `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                              `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                              `delete_time` DATETIME   COMMENT '删除时间' ,
                              PRIMARY KEY (id)
)  COMMENT = '模型供应商';

DROP TABLE IF EXISTS aacp_model;
CREATE TABLE aacp_model(
                           `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                           `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                           `name` VARCHAR(80) NOT NULL  COMMENT '模型变体名称' ,
                           `code` VARCHAR(64) NOT NULL  COMMENT '模型标识' ,
                           `kind` TINYINT NOT NULL  COMMENT '类型 0:文本 1:图形 2:音频 3:多模态' ,
                           `max_context` INT NOT NULL  COMMENT '最大上下文长度' ,
                           `max_output_token` INT NOT NULL  COMMENT '最大输出词元' ,
                           `api_reasoning` TINYINT NOT NULL  COMMENT '推理 0:不支持 1:支持' ,
                           `api_reasoning_effort` TINYINT NOT NULL  COMMENT '推理强度 0:关 1:低 2:中 3:高 4:极高' ,
                           `api_append_param` JSON NOT NULL  COMMENT '附加参数' ,
                           `api_append_headers` JSON NOT NULL  COMMENT '附加请求头' ,
                           `finc_input` DECIMAL(14,4) NOT NULL  COMMENT '输入单价' ,
                           `finc_input_cached` DECIMAL(14,4) NOT NULL  COMMENT '输入单价(缓存)' ,
                           `finc_output` DECIMAL(14,4) NOT NULL  COMMENT '输出单价' ,
                           `test_ttfb` INT   COMMENT '测试首字响应时间 MS' ,
                           `test_rate` INT   COMMENT '测试响应速率 T/S' ,
                           `test_time` DATETIME   COMMENT '最后测试时间' ,
                           `remark` VARCHAR(200)   COMMENT '备注' ,
                           `seq` INT NOT NULL  COMMENT '排序' ,
                           `status` TINYINT NOT NULL  COMMENT '状态 0:禁用 1:启用' ,
                           `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                           `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                           `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                           `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                           `delete_time` DATETIME   COMMENT '删除时间' ,
                           PRIMARY KEY (id)
)  COMMENT = '模型变体表';

DROP TABLE IF EXISTS aacp_provider_model;
CREATE TABLE aacp_provider_model(
                                    `provider_id` BIGINT NOT NULL  COMMENT 'PID' ,
                                    `model_id` BIGINT NOT NULL  COMMENT 'MID' ,
                                    PRIMARY KEY (provider_id,model_id)
)  COMMENT = 'APM表';

DROP TABLE IF EXISTS aacp_app;
CREATE TABLE aacp_app(
                         `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                         `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                         `name` VARCHAR(40) NOT NULL  COMMENT '应用名称' ,
                         `code` VARCHAR(16) NOT NULL  COMMENT '应用代码' ,
                         `app_key` VARCHAR(2048) NOT NULL  COMMENT '访问密钥' ,
                         `is_public` TINYINT NOT NULL  COMMENT '是否公开 0:不公开 1:公开' ,
                         `ips` JSON NOT NULL  COMMENT 'IP白名单列表' ,
                         `remark` VARCHAR(200)   COMMENT '备注' ,
                         `status` TINYINT NOT NULL  COMMENT '状态 0:禁用 1:启用' ,
                         `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                         `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                         `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                         `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                         `delete_time` DATETIME   COMMENT '删除时间' ,
                         PRIMARY KEY (id)
)  COMMENT = '应用表';
DROP TABLE IF EXISTS aacp_app_model;
CREATE TABLE aacp_app_model(
                               `app_id` BIGINT NOT NULL  COMMENT 'AID' ,
                               `model_id` BIGINT NOT NULL  COMMENT 'MID' ,
                               PRIMARY KEY (app_id,model_id)
)  COMMENT = 'AAM表';

DROP TABLE IF EXISTS aacp_app_logs;
CREATE TABLE aacp_app_logs(
                              `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                              `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                              `app_id` BIGINT NOT NULL  COMMENT '应用ID' ,
                              `provider_id` BIGINT NOT NULL  COMMENT '供应商ID' ,
                              `model_id` BIGINT NOT NULL  COMMENT '模型变体ID' ,
                              `input_token` INT   COMMENT '输入词元' ,
                              `output_token` INT   COMMENT '输出词元' ,
                              `cost` DECIMAL(22,8)   COMMENT '消耗金额' ,
                              `start_time` DATETIME NOT NULL  COMMENT '发起时间' ,
                              `end_time` DATETIME   COMMENT '结束时间' ,
                              `duration_ms` INT   COMMENT '总耗时MS' ,
                              `ttfb_ms` INT   COMMENT '首字响应时间' ,
                              `status_code` VARCHAR(40)   COMMENT 'HTTP状态码' ,
                              `client_ip` VARCHAR(45) NOT NULL  COMMENT '客户端IP' ,
                              `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                              `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                              `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                              `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                              `delete_time` DATETIME   COMMENT '删除时间' ,
                              PRIMARY KEY (id)
)  COMMENT = '调用日志流水表';
