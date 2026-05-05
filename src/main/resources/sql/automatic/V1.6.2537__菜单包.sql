DROP TABLE IF EXISTS core_pack;
CREATE TABLE core_pack(
                          `id` BIGINT NOT NULL  COMMENT '主键ID' ,
                          `name` VARCHAR(40) NOT NULL  COMMENT '菜单包名' ,
                          `code` VARCHAR(16) NOT NULL  COMMENT '菜单包编码' ,
                          `status` TINYINT NOT NULL  COMMENT '状态 0:禁用 1:启用' ,
                          `seq` INT NOT NULL  COMMENT '排序' ,
                          `remark` VARCHAR(200)   COMMENT '备注' ,
                          `create_time` DATETIME NOT NULL  COMMENT '创建时间' ,
                          `creator_id` BIGINT NOT NULL  COMMENT '创建人ID' ,
                          `update_time` DATETIME NOT NULL  COMMENT '更新时间' ,
                          `updater_id` BIGINT NOT NULL  COMMENT '更新人ID' ,
                          `delete_time` DATETIME   COMMENT '删除时间' ,
                          PRIMARY KEY (id)
)  COMMENT = '菜单包表';

DROP TABLE IF EXISTS core_menu_pack;
CREATE TABLE core_menu_pack(
                               `menu_id` BIGINT NOT NULL  COMMENT '菜单ID' ,
                               `pack_id` BIGINT NOT NULL  COMMENT '菜单包ID' ,
                               PRIMARY KEY (menu_id,pack_id)
)  COMMENT = 'MP表';

DROP TABLE IF EXISTS core_root_pack;
CREATE TABLE core_root_pack(
                               `root_id` BIGINT NOT NULL  COMMENT '租户ID' ,
                               `pack_id` BIGINT NOT NULL  COMMENT '菜单包ID' ,
                               PRIMARY KEY (root_id,pack_id)
)  COMMENT = 'RP表';
