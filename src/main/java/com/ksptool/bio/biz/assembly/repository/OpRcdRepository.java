package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.oprcd.OpRcdPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author KspTooi
 * @since 1.7.10(J).1
 */
@Repository
public interface OpRcdRepository extends JpaRepository<OpRcdPo, Long> {

    @Query("""
    SELECT u FROM OpRcdPo u
    WHERE
    (:#{#po.opName} IS NULL OR u.opName LIKE CONCAT('%', :#{#po.opName}, '%'))
    AND (:#{#po.dsName} IS NULL OR u.dsName LIKE CONCAT('%', :#{#po.dsName}, '%'))
    AND (:#{#po.dsTableName} IS NULL OR u.dsTableName LIKE CONCAT('%', :#{#po.dsTableName}, '%'))
    AND (:#{#po.modelName} IS NULL OR u.modelName LIKE CONCAT('%', :#{#po.modelName}, '%'))
    AND (:#{#po.bizDomain} IS NULL OR u.bizDomain LIKE CONCAT('%', :#{#po.bizDomain}, '%'))
    AND (:#{#po.creatorUsername} IS NULL OR u.creatorUsername LIKE CONCAT('%', :#{#po.creatorUsername}, '%'))
    ORDER BY u.createTime DESC
    """)
    Page<OpRcdPo> getOpRcdList(@Param("po") OpRcdPo po, Pageable pageable);
}
