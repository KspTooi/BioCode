package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpMcpCapabilityPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AacpMcpCapabilityRepository extends JpaRepository<AacpMcpCapabilityPo, AacpMcpCapabilityPo.Pk> {

    @Query("""
            SELECT COUNT(u) FROM AacpMcpCapabilityPo u WHERE u.capabilityId = :capabilityId
            """)
    long countByCapabilityId(@Param("capabilityId") Long capabilityId);

    @Query("""
            SELECT u.mcpId, COUNT(u.capabilityId) FROM AacpMcpCapabilityPo u
            WHERE u.mcpId IN :mcpIds
            GROUP BY u.mcpId
            """)
    List<Object[]> countByMcpIds(@Param("mcpIds") List<Long> mcpIds);

    @Query("""
            SELECT u.capabilityId FROM AacpMcpCapabilityPo u WHERE u.mcpId = :mcpId
            """)
    List<Long> getCapabilityIdsByMcpId(@Param("mcpId") Long mcpId);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpMcpCapabilityPo u WHERE u.mcpId = :mcpId AND u.capabilityId IN :capabilityIds
            """)
    void removeByMcpIdAndCapabilityIds(@Param("mcpId") Long mcpId, @Param("capabilityIds") List<Long> capabilityIds);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpMcpCapabilityPo u WHERE u.mcpId = :mcpId
            """)
    void removeByMcpId(@Param("mcpId") Long mcpId);
}
