package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.agenthub.AacpAgentHubPo;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentHubRepository extends JpaRepository<AacpAgentHubPo, Long> {

    /**
     * 根据编码统计智能体枢纽数量，排除指定ID（id为null时不排除）
     *
     * @param code 唯一编码
     * @param id   排除的ID，可为null
     * @return 数量
     */
    @Query("""
            SELECT COUNT(t) FROM AacpAgentHubPo t
            WHERE t.code = :code AND (:#{#id} IS NULL OR t.id != :id)
            """)
    Long countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Query("""
            SELECT u.id AS id, u.name AS name, u.code AS code,
                   u.networkKind AS networkKind, u.authKind AS authKind,
                   u.authPsk AS authPsk, u.status AS status,
                   COALESCE((SELECT COUNT(mc.capabilityId) FROM AacpMcpCapPo mc WHERE mc.mcpId = u.id), 0) AS capabilityCount,
                   COALESCE((SELECT COUNT(cf.microFuncId) FROM AacpMcpCapPo mc
                             LEFT JOIN AacpCapMicroFuncPo cf ON mc.capabilityId = cf.capId
                             WHERE mc.mcpId = u.id), 0) AS funcCount
            FROM AacpAgentHubPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getAgentHubList(@Param("po") AacpAgentHubPo po, Pageable pageable);

    /**
     * 根据编码查询智能体枢纽
     *
     * @param code 唯一编码
     * @return 智能体枢纽
     */
    @Query("""
            SELECT u FROM AacpAgentHubPo u
            WHERE u.code = :code
            """)
    AacpAgentHubPo getByCode(@Param("code") String code);
}
