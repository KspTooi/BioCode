package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.cap.AacpCapPo;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapRepository extends JpaRepository<AacpCapPo, Long> {

    @Query("""
            SELECT u.id AS id, u.name AS name, u.kind AS kind, u.remark AS remark,
                   COALESCE((SELECT COUNT(cf.microFuncId) FROM AacpCapMicroFuncPo cf WHERE cf.capId = u.id), 0) AS funcCount,
                   COALESCE((SELECT COUNT(cd.datasourceId) FROM AacpCapDatasourcePo cd WHERE cd.capId = u.id), 0) AS datasourceCount
            FROM AacpCapPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.kind} IS NULL OR u.kind = :#{#po.kind} )
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getCapList(@Param("po") AacpCapPo po, Pageable pageable);

    /**
     * 根据名称统计能力包数量，排除指定ID（id为null时不排除）
     *
     * @param name 能力包名称
     * @param id   排除的ID，可为null
     * @return 数量
     */
    @Query("""
            SELECT COUNT(t) FROM AacpCapPo t
            WHERE t.name = :name AND (:#{#id} IS NULL OR t.id != :id)
            """)
    int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);

    /**
     * 根据智能体枢纽ID获取已绑定的能力包列表
     *
     * @param hubId 智能体枢纽ID
     * @param kind  能力包类型 0:微函数
     * @return 能力包列表
     */
    @Query("""
            SELECT c FROM AacpCapPo c
            WHERE c.id IN (
                SELECT m.capId FROM AacpAgentHubCapPo m WHERE m.hubId = :hubId
            ) AND (:#{#kind} IS NULL OR c.kind = :#{#kind} )
            ORDER BY c.createTime DESC
            """)
    List<AacpCapPo> getByHubId(@Param("hubId") Long hubId, Integer kind);
}
