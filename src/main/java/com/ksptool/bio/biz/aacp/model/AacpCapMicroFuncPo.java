package com.ksptool.bio.biz.aacp.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@IdClass(AacpCapMicroFuncPo.Pk.class)
@Entity
@Table(name = "aacp_cap_micro_func", comment = "CMF表")
@AllArgsConstructor
@NoArgsConstructor
public class AacpCapMicroFuncPo implements Serializable {

    @Id
    @Column(name = "cap_id", nullable = false, comment = "能力包ID")
    private Long capId;

    @Id
    @Column(name = "micro_func_id", nullable = false, comment = "微函数ID")
    private Long microFuncId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Pk implements Serializable {
        private Long capId;
        private Long microFuncId;
    }
}
