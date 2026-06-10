package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpCapabilityPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AacpCapabilityRepository extends JpaRepository<AacpCapabilityPo, Long> {

    @Query("""
            SELECT u FROM AacpCapabilityPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.kind} IS NULL OR u.kind = :#{#po.kind} )
            ORDER BY u.createTime DESC
            """)
    Page<AacpCapabilityPo> getAacpCapabilityList(@Param("po") AacpCapabilityPo po, Pageable pageable);

    /**
     * 根据名称统计能力包数量，排除指定ID（id为null时不排除）
     *
     * @param name 能力包名称
     * @param id   排除的ID，可为null
     * @return 数量
     */
    @Query("""
            SELECT COUNT(t) FROM AacpCapabilityPo t
            WHERE t.name = :name AND (:#{#id} IS NULL OR t.id != :id)
            """)
    int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);

    /**
     * 根据MCP服务器ID获取已绑定的能力包列表
     *
     * @param mcpId MCP服务器ID
     * @return 能力包列表
     */
    @Query("""
            SELECT c FROM AacpCapabilityPo c
            WHERE c.id IN (
                SELECT m.capabilityId FROM AacpMcpCapabilityPo m WHERE m.mcpId = :mcpId
            )
            ORDER BY c.createTime DESC
            """)
    List<AacpCapabilityPo> getByMcpId(@Param("mcpId") Long mcpId);
}
