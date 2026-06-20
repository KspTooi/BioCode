package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.providermodel.ProviderModelPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProviderModelRepository extends JpaRepository<ProviderModelPo, Long>{

    @Query("""
    SELECT u FROM ProviderModelPo u
    WHERE
    ORDER BY u.createTime DESC
    """)
    Page<ProviderModelPo> getProviderModelList(@Param("po") ProviderModelPo po, Pageable pageable);
}
