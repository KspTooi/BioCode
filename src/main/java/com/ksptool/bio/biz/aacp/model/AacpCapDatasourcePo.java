package com.ksptool.bio.biz.aacp.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@IdClass(AacpCapDatasourcePo.Pk.class)
@Entity
@Table(name = "aacp_cap_datasource", comment = "CD表")
@AllArgsConstructor
@NoArgsConstructor
public class AacpCapDatasourcePo implements Serializable {

    @Id
    @Column(name = "cap_id", nullable = false, comment = "能力包ID")
    private Long capId;

    @Id
    @Column(name = "datasource_id", nullable = false, comment = "数据源ID")
    private Long datasourceId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long capId;
        private Long datasourceId;
    }
}
