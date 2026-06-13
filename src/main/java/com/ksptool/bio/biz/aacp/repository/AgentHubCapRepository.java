package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpAgentHubCapPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentHubCapRepository extends JpaRepository<AacpAgentHubCapPo, AacpAgentHubCapPo.Pk> {

    /**
     * 根据能力包ID统计智能体枢纽数量
     */
    @Query("""
            SELECT COUNT(u) FROM AacpAgentHubCapPo u WHERE u.capId = :capId
            """)
    long countByCapId(@Param("capId") Long capId);

    /**
     * 批量统计各智能体枢纽关联的能力包数量
     */
    @Query("""
            SELECT u.hubId, COUNT(u.capId) FROM AacpAgentHubCapPo u
            WHERE u.hubId IN :hubIds
            GROUP BY u.hubId
            """)
    List<Object[]> countByHubIds(@Param("hubIds") List<Long> hubIds);

    /**
     * 根据智能体枢纽ID获取已绑定的能力包ID列表
     */
    @Query("""
            SELECT u.capId FROM AacpAgentHubCapPo u WHERE u.hubId = :hubId
            """)
    List<Long> getCapIdsByHubId(@Param("hubId") Long hubId);

    /**
     * 根据智能体枢纽ID和能力包ID列表删除关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpAgentHubCapPo u WHERE u.hubId = :hubId AND u.capId IN :capIds
            """)
    void removeByHubIdAndCapIds(@Param("hubId") Long hubId, @Param("capIds") List<Long> capIds);

    /**
     * 根据智能体枢纽ID删除所有关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpAgentHubCapPo u WHERE u.hubId = :hubId
            """)
    void removeByHubId(@Param("hubId") Long hubId);
}
