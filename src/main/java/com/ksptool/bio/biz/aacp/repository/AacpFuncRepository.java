package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpFuncPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AacpFuncRepository extends JpaRepository<AacpFuncPo, Long> {

    @Query("""
            SELECT u FROM AacpFuncPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.description} IS NULL OR u.description LIKE CONCAT('%', :#{#po.description}, '%'))
            ORDER BY u.createTime DESC
            """)
    Page<AacpFuncPo> getAacpFuncList(@Param("po") AacpFuncPo po, Pageable pageable);
}
