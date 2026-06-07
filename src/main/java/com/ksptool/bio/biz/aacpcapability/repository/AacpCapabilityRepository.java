package com.ksptool.bio.biz.aacpcapability.repository;

import com.ksptool.bio.biz.aacpcapability.model.AacpCapabilityPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AacpCapabilityRepository extends JpaRepository<AacpCapabilityPo, Long>{

    @Query("""
    SELECT u FROM AacpCapabilityPo u
    WHERE
    (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
    AND (:#{#po.kind} IS NULL OR u.kind = :#{#po.kind} )
    ORDER BY u.createTime DESC
    """)
    Page<AacpCapabilityPo> getAacpCapabilityList(@Param("po") AacpCapabilityPo po, Pageable pageable);
}
