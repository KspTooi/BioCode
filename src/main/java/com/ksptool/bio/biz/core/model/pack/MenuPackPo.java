package com.ksptool.bio.biz.core.model.pack;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@IdClass(MenuPackPo.Pk.class)
@Entity
@Table(name = "core_menu_pack")
@AllArgsConstructor
@NoArgsConstructor
public class MenuPackPo implements Serializable {

    @Id
    @Column(name = "menu_id", nullable = false, comment = "菜单ID")
    private Long menuId;

    @Id
    @Column(name = "pack_id", nullable = false, comment = "菜单包ID")
    private Long packId;

    /**
     * 用于复合主键的类
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long menuId;
        private Long packId;
    }
}
