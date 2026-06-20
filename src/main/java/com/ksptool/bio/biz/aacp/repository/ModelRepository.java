package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.model.ModelPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ModelRepository extends JpaRepository<ModelPo, Long>{

    @Query("""
    SELECT u FROM ModelPo u
    WHERE
    (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
    AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
    AND (:#{#po.kind} IS NULL OR u.kind = :#{#po.kind} )
    AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
    ORDER BY u.createTime DESC
    """)
    Page<ModelPo> getModelList(@Param("po") ModelPo po, Pageable pageable);
}
