package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.polytemplate.PolyTemplatePo;
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
public interface PolyTemplateRepository extends JpaRepository<PolyTemplatePo, Long> {

    @Query("""
            SELECT u FROM PolyTemplatePo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<PolyTemplatePo> getPolyTemplateList(@Param("po") PolyTemplatePo po, Pageable pageable);

    /**
     * 根据ID统计聚合模板数量
     *
     * @param id 聚合模板ID
     * @return 聚合模板数量
     */
    @Query("""
            SELECT COUNT(u) FROM PolyTemplatePo u WHERE u.id = :id
            """)
    int countById(@Param("id") Long id);
}
