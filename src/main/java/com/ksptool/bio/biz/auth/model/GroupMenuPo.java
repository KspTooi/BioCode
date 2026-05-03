package com.ksptool.bio.biz.auth.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@IdClass(GroupMenuPo.Pk.class)
@Entity
@Table(name = "auth_group_menu")
public class GroupMenuPo implements Serializable {

    @Id
    @Column(name = "group_id", nullable = false, comment = "用户组ID")
    private Long groupId;

    @Id
    @Column(name = "menu_id", nullable = false, comment = "菜单ID")
    private Long menuId;

    /**
     * 用于复合主键的类
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long groupId;
        private Long menuId;
    }
}
