package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpMcpPo;
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
            SELECT u FROM AacpMcpPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.networkKind} IS NULL OR u.networkKind = :#{#po.networkKind} )
            AND (:#{#po.authPsk} IS NULL OR u.authPsk LIKE CONCAT('%', :#{#po.authPsk}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<AacpMcpPo> getAacpMcpList(@Param("po") AacpMcpPo po, Pageable pageable);
}
