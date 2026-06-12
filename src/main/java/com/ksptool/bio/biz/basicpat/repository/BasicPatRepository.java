package com.ksptool.bio.biz.basicpat.repository;

import com.ksptool.bio.biz.basicpat.model.BasicPatPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BasicPatRepository extends JpaRepository<BasicPatPo, Long>{

    @Query("""
    SELECT u FROM BasicPatPo u
    WHERE
    (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
    AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
    ORDER BY u.createTime DESC
    """)
    Page<BasicPatPo> getBasicPatList(@Param("po") BasicPatPo po, Pageable pageable);
}
