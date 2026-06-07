package com.ksptool.bio.biz.aacp.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@IdClass(AacpMcpCapabilityPo.Pk.class)
@Entity
@Table(name = "aacp_mcp_capability", comment = "MCP服务器-能力包关联表")
@AllArgsConstructor
@NoArgsConstructor
public class AacpMcpCapabilityPo implements Serializable {

    @Id
    @Column(name = "mcp_id", nullable = false, comment = "MCP服务器ID")
    private Long mcpId;

    @Id
    @Column(name = "capability_id", nullable = false, comment = "能力包ID")
    private Long capabilityId;

    /**
     * 用于复合主键的类
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long mcpId;
        private Long capabilityId;
    }
}
