package com.ksptool.bio.biz.audit.repository;

import com.ksptool.bio.biz.audit.model.auditerrorrcd.AuditErrorRcdPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditErrorRcdRepository extends JpaRepository<AuditErrorRcdPo, Long> {

    @Query("""
            SELECT u FROM AuditErrorRcdPo u
            WHERE
            (:#{#po.id} IS NULL OR u.id  = :#{#po.id} )
            AND (:#{#po.errorCode} IS NULL OR u.errorCode  LIKE CONCAT('%', :#{#po.errorCode}, '%') )
            AND (:#{#po.requestUri} IS NULL OR u.requestUri  LIKE CONCAT('%', :#{#po.requestUri}, '%') )
            AND (:#{#po.userId} IS NULL OR u.userId  = :#{#po.userId} )
            AND (:#{#po.userName} IS NULL OR u.userName  LIKE CONCAT('%', :#{#po.userName}, '%') )
            AND (:#{#po.errorType} IS NULL OR u.errorType  LIKE CONCAT('%', :#{#po.errorType}, '%') )
            AND (:#{#po.errorMessage} IS NULL OR u.errorMessage  LIKE CONCAT('%', :#{#po.errorMessage}, '%') )
            AND (:#{#po.errorStackTrace} IS NULL OR u.errorStackTrace  LIKE CONCAT('%', :#{#po.errorStackTrace}, '%') )
            AND (:#{#po.createTime} IS NULL OR u.createTime  = :#{#po.createTime} )
            ORDER BY u.createTime DESC
            """)
    Page<AuditErrorRcdPo> getAuditErrorRcdList(@Param("po") AuditErrorRcdPo po, Pageable pageable);
}
