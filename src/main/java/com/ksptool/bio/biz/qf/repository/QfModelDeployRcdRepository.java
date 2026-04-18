package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.QfModelDeployRcdPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QfModelDeployRcdRepository extends JpaRepository<QfModelDeployRcdPo, Long> {

    @Query("""
            SELECT u FROM QfModelDeployRcdPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """ )
    Page<QfModelDeployRcdPo> getQfModelDeployRcdList(@Param("po") QfModelDeployRcdPo po, Pageable pageable);

    @Query("""
            SELECT u FROM QfModelDeployRcdPo u
            WHERE u.code = :code
            AND u.status = 0
            AND u.engProcessDefId IS NOT NULL
            ORDER BY u.version DESC
            """)
    List<QfModelDeployRcdPo> findLatestActiveByCode(@Param("code") String code, Pageable pageable);
}
