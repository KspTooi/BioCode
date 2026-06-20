package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.tymschema.TymSchemaPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Repository
public interface TymSchemaRepository extends JpaRepository<TymSchemaPo, Long> {

    @Query("""
            SELECT u FROM TymSchemaPo u
            WHERE
            (:#{#po.id} IS NULL OR u.id  = :#{#po.id} )
            AND (:#{#po.name} IS NULL OR u.name  LIKE CONCAT('%', :#{#po.name}, '%') )
            AND (:#{#po.code} IS NULL OR u.code  LIKE CONCAT('%', :#{#po.code}, '%') )
            AND (:#{#po.typeCount} IS NULL OR u.typeCount  = :#{#po.typeCount} )
            AND (:#{#po.defaultType} IS NULL OR u.defaultType LIKE CONCAT('%', :#{#po.defaultType}, '%') )
            AND (:#{#po.seq} IS NULL OR u.seq  = :#{#po.seq} )
            AND (:#{#po.remark} IS NULL OR u.remark  LIKE CONCAT('%', :#{#po.remark}, '%') )
            AND (:#{#po.createTime} IS NULL OR u.createTime  = :#{#po.createTime} )
            AND (:#{#po.creatorId} IS NULL OR u.creatorId  = :#{#po.creatorId} )
            AND (:#{#po.updateTime} IS NULL OR u.updateTime  = :#{#po.updateTime} )
            AND (:#{#po.updaterId} IS NULL OR u.updaterId  = :#{#po.updaterId} )
            ORDER BY u.seq ASC,u.createTime DESC
            """)
    Page<TymSchemaPo> getTymSchemaList(@Param("po") TymSchemaPo po, Pageable pageable);

    
    /**
     * 根据编码统计类型映射方案数量
     *
     * @param code 编码
     * @return 类型映射方案数量
     */
    @Query("""
            SELECT COUNT(u) FROM TymSchemaPo u WHERE u.code = :code
            """)
    int countByCode(@Param("code") String code);

    /**
     * 根据编码统计类型映射方案数量 排除指定ID
     *
     * @param code 编码
     * @param id   类型映射方案ID
     * @return 类型映射方案数量
     */
    @Query("""
            SELECT COUNT(u) FROM TymSchemaPo u WHERE u.code = :code AND u.id != :id
            """)    
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);
}
