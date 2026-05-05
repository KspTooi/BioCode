package com.ksptool.bio.biz.core.model.pack;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@IdClass(RootPackPo.Pk.class)
@Entity
@Table(name = "core_root_pack")
@AllArgsConstructor
@NoArgsConstructor
public class RootPackPo implements Serializable {

    @Id
    @Column(name = "root_id", nullable = false, comment = "租户ID")
    private Long rootId;

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
        private Long rootId;
        private Long packId;
    }
}
