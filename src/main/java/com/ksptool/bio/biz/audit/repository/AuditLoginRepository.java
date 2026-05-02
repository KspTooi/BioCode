package com.ksptool.bio.biz.audit.repository;

import com.ksptool.bio.biz.audit.model.auditlogin.AuditLoginPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLoginRepository extends JpaRepository<AuditLoginPo, Long> {

    @Query("""
            SELECT u FROM AuditLoginPo u
            WHERE
            (:#{#po.id} IS NULL OR u.id  = :#{#po.id} )
            AND (:#{#po.userId} IS NULL OR u.userId  = :#{#po.userId} )
            AND (:#{#po.username} IS NULL OR u.username  LIKE CONCAT('%', :#{#po.username}, '%') )
            AND (:#{#po.loginKind} IS NULL OR u.loginKind  = :#{#po.loginKind} )
            AND (:#{#po.ipAddr} IS NULL OR u.ipAddr  LIKE CONCAT('%', :#{#po.ipAddr}, '%') )
            AND (:#{#po.location} IS NULL OR u.location  LIKE CONCAT('%', :#{#po.location}, '%') )
            AND (:#{#po.browser} IS NULL OR u.browser  LIKE CONCAT('%', :#{#po.browser}, '%') )
            AND (:#{#po.os} IS NULL OR u.os  LIKE CONCAT('%', :#{#po.os}, '%') )
            AND (:#{#po.message} IS NULL OR u.message  LIKE CONCAT('%', :#{#po.message}, '%') )
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status})
            AND (:#{#po.createTime} IS NULL OR u.createTime  = :#{#po.createTime} )
            ORDER BY u.createTime DESC
            """)
    Page<AuditLoginPo> getAuditLoginList(@Param("po") AuditLoginPo po, Pageable pageable);
}
