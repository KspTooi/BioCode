package com.ksptool.bio.biz.aacp.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@IdClass(AacpProviderModelPo.Pk.class)
@Entity
@Table(name = "aacp_provider_model", comment = "APM表")
public class AacpProviderModelPo {

    @Id
    @Column(name = "provider_id", nullable = false, comment = "PID")
    private Long providerId;

    @Id
    @Column(name = "model_id", nullable = false, comment = "MID")
    private Long modelId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long providerId;
        private Long modelId;
    }
}