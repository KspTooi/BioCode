SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Drop all old tables
-- ----------------------------
DROP TABLE IF EXISTS aacp_mcp;
DROP TABLE IF EXISTS aacp_capability;
DROP TABLE IF EXISTS accp_func;
DROP TABLE IF EXISTS aacp_mcp_capability;
DROP TABLE IF EXISTS aacp_capability_func;


-- ----------------------------
-- Table structure for aacp_agent_hub
-- ----------------------------
DROP TABLE IF EXISTS aacp_agent_hub;
CREATE TABLE aacp_agent_hub(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `name` VARCHAR(40) NOT NULL  COMMENT '服务器名称' ,
    `code` VARCHAR(16) NOT NULL  COMMENT '唯一编码' ,
    `network_kind` TINYINT NOT NULL  COMMENT '通信协议 0:HTTP+SSE 1:WS' ,
    `auth_kind` TINYINT NOT NULL  COMMENT '鉴权类型 0:无 1:PSK' ,
    `auth_psk` VARCHAR(2000)   COMMENT '预共享密钥' ,
    `status` TINYINT NOT NULL  COMMENT '状态 0:离线 1:在线' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间' ,
    PRIMARY KEY (id)
)  COMMENT = '智能体枢纽表';

-- ----------------------------
-- Table structure for aacp_cap
-- ----------------------------
DROP TABLE IF EXISTS aacp_cap;
CREATE TABLE aacp_cap(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `name` VARCHAR(40) NOT NULL  COMMENT '能力包名称' ,
    `kind` TINYINT NOT NULL  COMMENT '类型 0:微函数' ,
    `remark` VARCHAR(500)   COMMENT '备注' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间' ,
    PRIMARY KEY (id)
)  COMMENT = '能力包表';

-- ----------------------------
-- Table structure for accp_micro_func
-- ----------------------------
DROP TABLE IF EXISTS accp_micro_func;
CREATE TABLE accp_micro_func(
    `id` BIGINT NOT NULL  COMMENT '主键ID' ,
    `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
    `name` VARCHAR(40) NOT NULL  COMMENT '微函数名称' ,
    `code` VARCHAR(32) NOT NULL  COMMENT '微函数标识' ,
    `description` VARCHAR(1000) NOT NULL  COMMENT '意图词' ,
    `schema` JSON   COMMENT '入参规范' ,
    `target` VARCHAR(1000) NOT NULL  COMMENT '调用目标Bean' ,
    `remark` VARCHAR(500)   COMMENT '备注' ,
    `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
    `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
    `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
    `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
    `delete_time` DATETIME   COMMENT '删除时间' ,
    PRIMARY KEY (id)
)  COMMENT = '微函数表';

-- ----------------------------
-- Table structure for aacp_cap_micro_func
-- ----------------------------
DROP TABLE IF EXISTS aacp_cap_micro_func;
CREATE TABLE aacp_cap_micro_func(
    `cap_id` BIGINT NOT NULL  COMMENT '能力包ID' ,
    `micro_func_id` BIGINT NOT NULL  COMMENT '微函数ID' ,
    PRIMARY KEY (cap_id,micro_func_id)
)  COMMENT = 'CMF表';

-- ----------------------------
-- Table structure for accp_agent_hub_cap
-- ----------------------------
DROP TABLE IF EXISTS accp_agent_hub_cap;
CREATE TABLE accp_agent_hub_cap(
    `hub_id` BIGINT NOT NULL  COMMENT '智能体枢纽ID' ,
    `cap_id` BIGINT NOT NULL  COMMENT '能力包ID' ,
    PRIMARY KEY (hub_id,cap_id)
)  COMMENT = 'AHC表';

SET FOREIGN_KEY_CHECKS = 1;
