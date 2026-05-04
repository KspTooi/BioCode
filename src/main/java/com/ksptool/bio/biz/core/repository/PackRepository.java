package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.pack.PackPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PackRepository extends JpaRepository<PackPo, Long>{

    @Query("""
    SELECT u FROM PackPo u
    WHERE
    (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
    AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
    AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
    ORDER BY u.createTime DESC
    """)
    Page<PackPo> getPackList(@Param("po") PackPo po, Pageable pageable);
}
