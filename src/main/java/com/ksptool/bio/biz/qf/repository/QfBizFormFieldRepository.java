package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qfbizformfield.QfBizFormFieldPo;
import com.ksptool.bio.biz.qf.model.qfbizformfield.dto.GetQfBizFormFieldListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface QfBizFormFieldRepository extends JpaRepository<QfBizFormFieldPo, Long>{

    @Query("""
            SELECT q FROM QfBizFormFieldPo q 
            WHERE (:#{#po.formId} IS NULL OR q.formId = :#{#po.formId})
            ORDER BY q.fieldName ASC
            """)
    Page<QfBizFormFieldPo> getQfBizFormFieldList(@Param("po") GetQfBizFormFieldListDto po, Pageable pageable);

    @Query("SELECT COUNT(q) > 0 FROM QfBizFormFieldPo q WHERE q.formId = :formId AND q.fieldName = :fieldName")
    boolean existsByFormIdAndFieldName(@Param("formId") Long formId, @Param("fieldName") String fieldName);

    @Query("SELECT COUNT(q) > 0 FROM QfBizFormFieldPo q WHERE q.formId = :formId AND q.fieldName = :fieldName AND q.id != :id")
    boolean existsByFormIdAndFieldNameAndIdNot(@Param("formId") Long formId, @Param("fieldName") String fieldName, @Param("id") Long id);
}
