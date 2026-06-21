package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.provider.AacpProviderPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<AacpProviderPo, Long> {

    @Query("""
            SELECT u FROM AacpProviderPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<AacpProviderPo> getProviderList(@Param("po") AacpProviderPo po, Pageable pageable);

    @Query("SELECT COUNT(u) FROM AacpProviderPo u WHERE u.code = :code AND (:id IS NULL OR u.id <> :id)")
    Long countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    /**
     * 根据模型ID获取该模型的第一个可用供应商
     *
     * @param modelId 模型ID
     * @return 供应商
     */
    @Query("""
            SELECT u FROM AacpProviderPo u
            INNER JOIN AacpProviderModelPo apm ON apm.providerId = u.id
            WHERE apm.modelId = :modelId
            ORDER BY u.createTime DESC
            LIMIT 1
            """)
    AacpProviderPo getFirstProviderByModelId(@Param("modelId") Long modelId);
}
