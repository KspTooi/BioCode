package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpCapabilityFuncPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AacpCapabilityFuncRepository extends JpaRepository<AacpCapabilityFuncPo, AacpCapabilityFuncPo.Pk> {

    @Query("""
            SELECT COUNT(u) FROM AacpCapabilityFuncPo u WHERE u.funcId = :funcId
            """)
    long countByFuncId(@Param("funcId") Long funcId);

    /**
     * 根据能力包ID获取已绑定的微函数ID列表
     */
    @Query("""
            SELECT u.funcId FROM AacpCapabilityFuncPo u WHERE u.capabilityId = :capabilityId
            """)
    List<Long> getFidsByCid(@Param("capabilityId") Long capabilityId);

    /**
     * 根据能力包ID和微函数ID列表删除关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpCapabilityFuncPo u WHERE u.capabilityId = :capabilityId AND u.funcId IN :funcIds
            """)
    void removeByCidAndFids(@Param("capabilityId") Long capabilityId, @Param("funcIds") List<Long> funcIds);

    /**
     * 根据能力包ID删除所有关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpCapabilityFuncPo u WHERE u.capabilityId = :capabilityId
            """)
    void removeByCapabilityId(@Param("capabilityId") Long capabilityId);
}
