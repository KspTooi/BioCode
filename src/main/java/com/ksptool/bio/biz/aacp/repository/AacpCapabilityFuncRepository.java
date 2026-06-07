package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpCapabilityFuncPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AacpCapabilityFuncRepository extends JpaRepository<AacpCapabilityFuncPo, AacpCapabilityFuncPo.Pk> {

    @Query("""
            SELECT COUNT(u) FROM AacpCapabilityFuncPo u WHERE u.funcId = :funcId
            """)
    long countByFuncId(@Param("funcId") Long funcId);

}
