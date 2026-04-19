package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qfmodel.QfModelPo;
import com.ksptool.bio.biz.qf.model.qfmodel.dto.GetQfModelListDto;
import com.ksptool.bio.biz.qf.model.qfmodel.vo.GetQfModelListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 流程模型Repository
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-16
 */
@Repository
public interface QfModelRepository extends JpaRepository<QfModelPo, Long> {

    @Query("""
            SELECT new com.ksptool.bio.biz.qf.model.qfmodel.vo.GetQfModelListVo(
                u.id,
                g.name,
                u.name,
                u.code,
                u.version,
                u.status,
                u.seq,
                u.createTime
            )
            FROM QfModelPo u
            LEFT JOIN QfModelGroupPo g ON g.id = u.groupId
            WHERE
            (:#{#dto.name} IS NULL OR u.name LIKE CONCAT('%', :#{#dto.name}, '%'))
            AND (:#{#dto.code} IS NULL OR u.code LIKE CONCAT('%', :#{#dto.code}, '%'))
            AND (:#{#dto.groupName} IS NULL OR g.name LIKE CONCAT('%', :#{#dto.groupName}, '%'))
            AND (:#{#dto.status} IS NULL OR u.status IN :#{#dto.status})
            ORDER BY u.seq ASC, u.createTime DESC
            """)
    Page<GetQfModelListVo> getQfModelList(@Param("dto") GetQfModelListDto dto, Pageable pageable);

    /**
     * 根据编码统计流程模型数量 排除指定ID
     *
     * @param code 流程模型编码
     * @param id   流程模型ID
     * @return 流程模型数量
     */
    @Query("""
            SELECT COUNT(t) FROM QfModelPo t
            WHERE t.code = :code AND (:id IS NULL OR t.id != :id)
            """)
    Long countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    /**
     * 根据模型分组ID统计流程模型数量
     *
     * @param groupId 模型分组ID
     * @return 流程模型数量
     */
    @Query("""
            SELECT COUNT(u) FROM QfModelPo u
            WHERE u.groupId = :groupId
            """)
    int countByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据模型ID查询是否有部署物
     *
     * @param modelId 模型ID
     * @return 是否有部署物
     */
    @Query("""
            SELECT COUNT(u) FROM QfModelDeployRcdPo u
            WHERE u.modelId = :modelId
            """)
    int countDeployByModelId(@Param("modelId") Long modelId);

    /**
     * 根据模型ID和版本查询是否有活跃的部署
     *
     * @param modelId 模型ID
     * @param version 版本
     * @return 是否有活跃的部署
     */
    @Query("""
            SELECT COUNT(u) FROM QfModelDeployRcdPo u
            WHERE u.modelId = :modelId AND u.version = :version AND u.status = 0
            """)
    int countActiveDeployByModelIdAndVersion(@Param("modelId") Long modelId, @Param("version") Integer version);
}
