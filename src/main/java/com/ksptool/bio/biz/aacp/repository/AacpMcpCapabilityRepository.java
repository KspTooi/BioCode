package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpMcpCapabilityPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AacpMcpCapabilityRepository extends JpaRepository<AacpMcpCapabilityPo, AacpMcpCapabilityPo.Pk> {

    @Query("""
            SELECT COUNT(u) FROM AacpMcpCapabilityPo u WHERE u.capabilityId = :capabilityId
            """)
    long countByCapabilityId(@Param("capabilityId") Long capabilityId);
}
