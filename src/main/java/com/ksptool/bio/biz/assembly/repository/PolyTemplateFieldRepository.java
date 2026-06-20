package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.polytemplatefield.PolyTemplateFieldPo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author KspTooi
 * @since 1.7.9(I).1
 */
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

    /**
     * 根据聚合模板ID查找APTF
     *
     * @param polyTemplateId 聚合模板ID
     * @return APTF
     */
    @Query("""
            SELECT u FROM PolyTemplateFieldPo u 
            WHERE u.polyTemplateId = :polyTemplateId 
            ORDER BY u.seq ASC
            """)
    List<PolyTemplateFieldPo> getAptfByAptId(@Param("polyTemplateId") Long polyTemplateId);
}
