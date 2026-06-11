package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpMcpCapPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface McpCapRepository extends JpaRepository<AacpMcpCapPo, AacpMcpCapPo.Pk> {

    /**
     * 根据能力包ID统计MCP服务器数量
     *
     * @param capabilityId 能力包ID
     * @return 数量
     */
    @Query("""
            SELECT COUNT(u) FROM AacpMcpCapPo u WHERE u.capabilityId = :capabilityId
            """)
    long countByCapabilityId(@Param("capabilityId") Long capabilityId);

    /**
     * 批量统计各MCP服务器关联的能力包数量
     *
     * @param mcpIds MCP服务器ID列表
     * @return [mcpId, count] 数组列表
     */
    @Query("""
            SELECT u.mcpId, COUNT(u.capabilityId) FROM AacpMcpCapPo u
            WHERE u.mcpId IN :mcpIds
            GROUP BY u.mcpId
            """)
    List<Object[]> countByMcpIds(@Param("mcpIds") List<Long> mcpIds);

    /**
     * 根据MCP服务器ID获取已绑定的能力包ID列表
     *
     * @param mcpId MCP服务器ID
     * @return 能力包ID列表
     */
    @Query("""
            SELECT u.capabilityId FROM AacpMcpCapPo u WHERE u.mcpId = :mcpId
            """)
    List<Long> getCapabilityIdsByMcpId(@Param("mcpId") Long mcpId);

    /**
     * 根据MCP服务器ID和能力包ID列表删除关联
     *
     * @param mcpId         MCP服务器ID
     * @param capabilityIds 能力包ID列表
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpMcpCapPo u WHERE u.mcpId = :mcpId AND u.capabilityId IN :capabilityIds
            """)
    void removeByMcpIdAndCapabilityIds(@Param("mcpId") Long mcpId, @Param("capabilityIds") List<Long> capabilityIds);

    /**
     * 根据MCP服务器ID删除所有关联
     *
     * @param mcpId MCP服务器ID
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpMcpCapPo u WHERE u.mcpId = :mcpId
            """)
    void removeByMcpId(@Param("mcpId") Long mcpId);
}
