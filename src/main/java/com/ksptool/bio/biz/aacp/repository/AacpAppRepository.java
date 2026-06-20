package com.ksptool.bio.biz.aacp.repository;


import com.ksptool.bio.biz.aacp.model.app.AacpAppPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AacpAppRepository extends JpaRepository<AacpAppPo, Long> {

    @Query("""
            SELECT u FROM AacpAppPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<AacpAppPo> getAacpAppList(@Param("po") AacpAppPo po, Pageable pageable);
}
