package com.ksptool.bio.biz.aacp.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@IdClass(AacpAgentHubCapPo.Pk.class)
@Entity
@Table(name = "accp_agent_hub_cap", comment = "AHC表")
@AllArgsConstructor
@NoArgsConstructor
public class AacpAgentHubCapPo implements Serializable {

    @Id
    @Column(name = "hub_id", nullable = false, comment = "智能体枢纽ID")
    private Long hubId;

    @Id
    @Column(name = "cap_id", nullable = false, comment = "能力包ID")
    private Long capId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long hubId;
        private Long capId;
    }
}
