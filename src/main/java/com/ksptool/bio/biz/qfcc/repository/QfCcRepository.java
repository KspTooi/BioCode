package com.ksptool.bio.biz.qfcc.repository;

import com.ksptool.bio.biz.qfcc.model.QfCcPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface QfCcRepository extends JpaRepository<QfCcPo, Long>{

    @Query("""
    SELECT u FROM QfCcPo u
    WHERE
    (:#{#po.summary} IS NULL OR u.summary LIKE CONCAT('%', :#{#po.summary}, '%'))
    AND (:#{#po.fromName} IS NULL OR u.fromName LIKE CONCAT('%', :#{#po.fromName}, '%'))
    AND (:#{#po.isRead} IS NULL OR u.isRead = :#{#po.isRead} )
    ORDER BY u.createTime DESC
    """)
    Page<QfCcPo> getQfCcList(@Param("po") QfCcPo po, Pageable pageable);
}
