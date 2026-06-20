package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.provider.ProviderPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProviderRepository extends JpaRepository<ProviderPo, Long>{

    @Query("""
    SELECT u FROM ProviderPo u
    WHERE
    (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
    AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
    AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
    ORDER BY u.createTime DESC
    """)
    Page<ProviderPo> getProviderList(@Param("po") ProviderPo po, Pageable pageable);
}
