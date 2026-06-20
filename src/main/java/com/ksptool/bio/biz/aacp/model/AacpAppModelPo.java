package com.ksptool.bio.biz.aacp.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@IdClass(AacpAppModelPo.Pk.class)
@Entity
@Table(name = "aacp_app_model", comment = "AAM表")
public class AacpAppModelPo {

    @Id
    @Column(name = "app_id", nullable = false, comment = "AID")
    private Long appId;

    @Id
    @Column(name = "model_id", nullable = false, comment = "MID")
    private Long modelId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long appId;
        private Long modelId;
    }
}