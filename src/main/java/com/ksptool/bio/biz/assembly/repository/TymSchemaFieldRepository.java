package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.tymschemafield.TymSchemaFieldPo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Repository
public interface TymSchemaFieldRepository extends JpaRepository<TymSchemaFieldPo, Long> {

    @Query("""
            SELECT u FROM TymSchemaFieldPo u
            WHERE
            (:#{#po.typeSchemaId} IS NULL OR u.typeSchemaId  = :#{#po.typeSchemaId} )
            ORDER BY u.seq,u.createTime DESC
            """)
    Page<TymSchemaFieldPo> getTymSchemaFieldList(@Param("po") TymSchemaFieldPo po, Pageable pageable);

    /**
     * 查询类型映射方案绑定的全部映射方案字段
     *
     * @param tymSid 类型映射方案ID
     * @return 类型映射方案绑定的全部映射方案字段
     */
    @Query("""
            SELECT u FROM TymSchemaFieldPo u
            WHERE
            u.typeSchemaId = :tymSid
            ORDER BY u.seq ASC
            """)
    List<TymSchemaFieldPo> getTymSfByTymSid(Long tymSid);
}
