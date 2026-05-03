DROP TABLE IF EXISTS auth_group_menu;
CREATE TABLE auth_group_menu(
                                `group_id` BIGINT NOT NULL  COMMENT '用户组ID' ,
                                `menu_id` BIGINT NOT NULL  COMMENT '菜单ID' ,
                                PRIMARY KEY (group_id,menu_id)
)  COMMENT = 'GM表';

CREATE UNIQUE INDEX uk_gm ON auth_group_menu(group_id,menu_id);
