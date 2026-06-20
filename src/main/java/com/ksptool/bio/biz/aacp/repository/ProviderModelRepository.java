package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.providermodel.ProviderModelPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderModelRepository extends JpaRepository<ProviderModelPo, Long> {


}
