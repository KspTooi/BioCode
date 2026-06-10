package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpMcpPo;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AacpMcpRepository extends JpaRepository<AacpMcpPo, Long> {

    /**
     * 根据编码统计MCP服务器数量，排除指定ID（id为null时不排除）
     *
     * @param code 唯一编码
     * @param id   排除的ID，可为null
     * @return 数量
     */
    @Query("""
            SELECT COUNT(t) FROM AacpMcpPo t
            WHERE t.code = :code AND (:#{#id} IS NULL OR t.id != :id)
            """)
    Long countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Query("""
            SELECT u.id AS id, u.name AS name, u.code AS code,
                   u.networkKind AS networkKind, u.authKind AS authKind,
                   u.authPsk AS authPsk, u.status AS status,
                   COALESCE((SELECT COUNT(mc.capabilityId) FROM AacpMcpCapabilityPo mc WHERE mc.mcpId = u.id), 0) AS capabilityCount,
                   COALESCE((SELECT COUNT(cf.funcId) FROM AacpMcpCapabilityPo mc
                             LEFT JOIN AacpCapabilityFuncPo cf ON mc.capabilityId = cf.capabilityId
                             WHERE mc.mcpId = u.id), 0) AS funcCount
            FROM AacpMcpPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getAacpMcpList(@Param("po") AacpMcpPo po, Pageable pageable);

    /**
     * 根据编码查询MCP服务器
     *
     * @param code 唯一编码
     * @return MCP服务器
     */
    @Query("""
            SELECT u FROM AacpMcpPo u
            WHERE u.code = :code
            """)
    AacpMcpPo getByCode(@Param("code") String code);
}
