package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpProviderModelPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderModelRepository extends JpaRepository<AacpProviderModelPo, AacpProviderModelPo.Pk> {

    /**
     * 根据模型变体ID获取已绑定的供应商ID列表
     */
    @Query("""
            SELECT u.providerId FROM AacpProviderModelPo u WHERE u.modelId = :modelId
            """)
    List<Long> getProviderIdsByModelId(@Param("modelId") Long modelId);

    /**
     * 根据模型变体ID和供应商ID列表删除关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpProviderModelPo u WHERE u.modelId = :modelId AND u.providerId IN :providerIds
            """)
    void removeByModelIdAndProviderIds(@Param("modelId") Long modelId, @Param("providerIds") List<Long> providerIds);

    /**
     * 根据模型变体ID删除所有关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpProviderModelPo u WHERE u.modelId = :modelId
            """)
    void removeByModelId(@Param("modelId") Long modelId);
}