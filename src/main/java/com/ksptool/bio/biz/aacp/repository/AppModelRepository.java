package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.appmodel.AppModelPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AppModelRepository extends JpaRepository<AppModelPo, Long>{

    @Query("""
    SELECT u FROM AppModelPo u
    WHERE
    ORDER BY u.createTime DESC
    """)
    Page<AppModelPo> getAppModelList(@Param("po") AppModelPo po, Pageable pageable);
}
