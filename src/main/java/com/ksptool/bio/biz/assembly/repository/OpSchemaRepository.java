package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.opschema.OpSchemaPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Repository
public interface OpSchemaRepository extends JpaRepository<OpSchemaPo, Long> {

    @Query("""
            SELECT u FROM OpSchemaPo u
            WHERE
            (:#{#po.id} IS NULL OR u.id  = :#{#po.id} )
            AND (:#{#po.dataSourceId} IS NULL OR u.dataSourceId  = :#{#po.dataSourceId} )
            AND (:#{#po.typeSchemaId} IS NULL OR u.typeSchemaId  = :#{#po.typeSchemaId} )
            AND (:#{#po.inputScmId} IS NULL OR u.inputScmId  = :#{#po.inputScmId} )
            AND (:#{#po.outputScmId} IS NULL OR u.outputScmId  = :#{#po.outputScmId} )
            AND (:#{#po.name} IS NULL OR u.name  LIKE CONCAT('%', :#{#po.name}, '%') )
            AND (:#{#po.modelName} IS NULL OR u.modelName  LIKE CONCAT('%', :#{#po.modelName}, '%') )
            AND (:#{#po.tableName} IS NULL OR u.tableName  LIKE CONCAT('%', :#{#po.tableName}, '%') )
            AND (:#{#po.removeTablePrefix} IS NULL OR u.removeTablePrefix  LIKE CONCAT('%', :#{#po.removeTablePrefix}, '%') )
            AND (:#{#po.permCodePrefix} IS NULL OR u.permCodePrefix  LIKE CONCAT('%', :#{#po.permCodePrefix}, '%') )
            AND (:#{#po.policyOverride} IS NULL OR u.policyOverride  = :#{#po.policyOverride} )
            AND (:#{#po.baseInput} IS NULL OR u.baseInput  LIKE CONCAT('%', :#{#po.baseInput}, '%') )
            AND (:#{#po.baseOutput} IS NULL OR u.baseOutput  LIKE CONCAT('%', :#{#po.baseOutput}, '%') )
            AND (:#{#po.remark} IS NULL OR u.remark  LIKE CONCAT('%', :#{#po.remark}, '%') )
            AND (:#{#po.fieldCountOrigin} IS NULL OR u.fieldCountOrigin  = :#{#po.fieldCountOrigin} )
            AND (:#{#po.fieldCountPoly} IS NULL OR u.fieldCountPoly  = :#{#po.fieldCountPoly} )
            AND (:#{#po.createTime} IS NULL OR u.createTime  = :#{#po.createTime} )
            AND (:#{#po.creatorId} IS NULL OR u.creatorId  = :#{#po.creatorId} )
            AND (:#{#po.updateTime} IS NULL OR u.updateTime  = :#{#po.updateTime} )
            AND (:#{#po.updaterId} IS NULL OR u.updaterId  = :#{#po.updaterId} )
            ORDER BY u.createTime DESC
            """)
    Page<OpSchemaPo> getOpSchemaList(@Param("po") OpSchemaPo po, Pageable pageable);

    /**
     * 根据名称查询输出方案（排除指定ID）
     */
    @Query("""
            SELECT COUNT(u) FROM OpSchemaPo u WHERE u.name = :name AND (:excludeId IS NULL OR u.id <> :excludeId)
            """)
    int countByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 根据类型映射方案ID统计输出方案数量
     *
     * @param typeSchemaId 类型映射方案ID
     * @return 输出方案数量
     */
    @Query("""
            SELECT COUNT(u) FROM OpSchemaPo u WHERE u.typeSchemaId = :typeSchemaId
            """)
    int countByTypeSchemaId(@Param("typeSchemaId") Long typeSchemaId);

    /**
     * 根据数据源ID统计输出方案数量
     *
     * @param dataSourceId 数据源ID
     * @return 输出方案数量
     */
    @Query("""
            SELECT COUNT(u) FROM OpSchemaPo u WHERE u.dataSourceId = :dataSourceId
            """)
    int countByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    /**
     * 更新输出方案的聚合模型数量
     *
     * @param outputSchemaId 输出方案ID
     * @return 更新条数
     */
    @Query("""
            UPDATE OpSchemaPo u SET u.fieldCountPoly = (SELECT COUNT(p) FROM PolyModelPo p WHERE p.outputSchemaId = :outputSchemaId) WHERE u.id = :outputSchemaId
            """)
    @Modifying
    int updatePolyModelCount(@Param("outputSchemaId") Long outputSchemaId);
}
