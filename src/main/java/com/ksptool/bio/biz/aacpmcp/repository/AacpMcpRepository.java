package com.ksptool.bio.biz.aacpmcp.repository;

import com.ksptool.bio.biz.aacpmcp.model.AacpMcpPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AacpMcpRepository extends JpaRepository<AacpMcpPo, Long>{

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
