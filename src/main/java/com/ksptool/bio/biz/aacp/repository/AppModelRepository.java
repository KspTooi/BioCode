package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpAppModelPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppModelRepository extends JpaRepository<AacpAppModelPo, AacpAppModelPo.Pk> {

    /**
     * 根据应用ID获取已绑定的模型变体ID列表
     */
    @Query("""
            SELECT u.modelId FROM AacpAppModelPo u WHERE u.appId = :appId
            """)
    List<Long> getModelIdsByAppId(@Param("appId") Long appId);

    /**
     * 根据应用ID和模型变体ID列表删除关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpAppModelPo u WHERE u.appId = :appId AND u.modelId IN :modelIds
            """)
    void removeByAppIdAndModelIds(@Param("appId") Long appId, @Param("modelIds") List<Long> modelIds);

    /**
     * 根据应用ID删除所有关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpAppModelPo u WHERE u.appId = :appId
            """)
    void removeByAppId(@Param("appId") Long appId);
}