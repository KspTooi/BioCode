package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpFuncPo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AacpFuncRepository extends JpaRepository<AacpFuncPo, Long> {

    @Query("""
            SELECT u FROM AacpFuncPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.description} IS NULL OR u.description LIKE CONCAT('%', :#{#po.description}, '%'))
            ORDER BY u.createTime DESC
            """)
    Page<AacpFuncPo> getAacpFuncList(@Param("po") AacpFuncPo po, Pageable pageable);

    /**
     * 根据标识统计微函数数量，排除指定ID（id为null时不排除）
     *
     * @param code 微函数标识
     * @param id   排除的ID，可为null
     * @return 数量
     */
    @Query("""
            SELECT COUNT(t) FROM AacpFuncPo t
            WHERE t.code = :code AND (:#{#id} IS NULL OR t.id != :id)
            """)
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    /**
     * 根据标识获取微函数
     *
     * @param code 微函数标识
     * @return 微函数，不存在返回null
     */
    @Query("""
            SELECT u FROM AacpFuncPo u WHERE u.code = :code
            """)
    AacpFuncPo getByCode(@Param("code") String code);

    /**
     * 根据能力包ID列表获取微函数列表
     *
     * @param ids 能力包ID列表
     * @return 微函数列表
     */
    @Query("""
            SELECT u FROM AacpFuncPo u
            WHERE u.id IN (
                SELECT cf.funcId FROM AacpCapabilityFuncPo cf WHERE cf.capabilityId IN :ids
            )
            ORDER BY u.createTime DESC
            """)
    List<AacpFuncPo> getFuncListByCapabilityIds(@Param("ids") Collection<Long> ids);

}
