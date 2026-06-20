package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.polytemplatefield.PolyTemplateFieldPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PolyTemplateFieldRepository extends JpaRepository<PolyTemplateFieldPo, Long> {

    @Query("""
            SELECT u FROM PolyTemplateFieldPo u
            WHERE
            (:#{#po.polyTemplateId} IS NULL OR u.polyTemplateId = :#{#po.polyTemplateId} )
            AND (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.policyCrudJson} IS NULL OR u.policyCrudJson LIKE CONCAT('%', :#{#po.policyCrudJson}, '%'))
            AND (:#{#po.policyQuery} IS NULL OR u.policyQuery = :#{#po.policyQuery} )
            AND (:#{#po.policyView} IS NULL OR u.policyView = :#{#po.policyView} )
            AND (:#{#po.seq} IS NULL OR u.seq = :#{#po.seq} )
            ORDER BY u.createTime DESC
            """)
    Page<PolyTemplateFieldPo> getPolyTemplateFieldList(@Param("po") PolyTemplateFieldPo po, Pageable pageable);
}
