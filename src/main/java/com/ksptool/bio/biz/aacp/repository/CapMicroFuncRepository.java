package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpCapMicroFuncPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapMicroFuncRepository extends JpaRepository<AacpCapMicroFuncPo, AacpCapMicroFuncPo.Pk> {

    @Query("""
            SELECT COUNT(u) FROM AacpCapMicroFuncPo u WHERE u.microFuncId = :microFuncId
            """)
    long countByMicroFuncId(@Param("microFuncId") Long microFuncId);

    /**
     * 根据能力包ID获取已绑定的微函数ID列表
     */
    @Query("""
            SELECT u.microFuncId FROM AacpCapMicroFuncPo u WHERE u.capId = :capId
            """)
    List<Long> getMicroFuncIdsByCapId(@Param("capId") Long capId);

    /**
     * 根据能力包ID和微函数ID列表删除关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpCapMicroFuncPo u WHERE u.capId = :capId AND u.microFuncId IN :microFuncIds
            """)
    void removeByCapIdAndMicroFuncIds(@Param("capId") Long capId, @Param("microFuncIds") List<Long> microFuncIds);

    /**
     * 根据能力包ID删除所有关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpCapMicroFuncPo u WHERE u.capId = :capId
            """)
    void removeByCapId(@Param("capId") Long capId);
}
