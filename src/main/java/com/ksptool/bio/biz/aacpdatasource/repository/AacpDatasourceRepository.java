package com.ksptool.bio.biz.aacpdatasource.repository;

import com.ksptool.bio.biz.aacpdatasource.model.AacpDatasourcePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AacpDatasourceRepository extends JpaRepository<AacpDatasourcePo, Long>{

    @Query("""
    SELECT u FROM AacpDatasourcePo u
    WHERE
    (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
    AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
    ORDER BY u.createTime DESC
    """)
    Page<AacpDatasourcePo> getAacpDatasourceList(@Param("po") AacpDatasourcePo po, Pageable pageable);
}
