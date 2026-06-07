package com.ksptool.bio.biz.aacp.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@IdClass(AacpCapabilityFuncPo.Pk.class)
@Entity
@Table(name = "aacp_capability_func", comment = "能力包-微函数关联表")
@AllArgsConstructor
@NoArgsConstructor
public class AacpCapabilityFuncPo implements Serializable {

    @Id
    @Column(name = "capability_id", nullable = false, comment = "能力包ID")
    private Long capabilityId;

    @Id
    @Column(name = "func_id", nullable = false, comment = "微函数ID")
    private Long funcId;

    /**
     * 用于复合主键的类
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long capabilityId;
        private Long funcId;
    }
}
