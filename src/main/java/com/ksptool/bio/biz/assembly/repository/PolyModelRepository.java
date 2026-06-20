package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.polymodel.PolyModelPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Repository
public interface PolyModelRepository extends JpaRepository<PolyModelPo, Long> {

    @Query("""
            SELECT u FROM PolyModelPo u
            WHERE
            (:#{#po.outputSchemaId} IS NULL OR u.outputSchemaId  = :#{#po.outputSchemaId} )
            ORDER BY u.seq ASC
            """)
    List<PolyModelPo> getPolyModelList(@Param("po") PolyModelPo po);

    /**
     * 清空输出方案聚合模型
     *
     * @param outputSchemaId 输出方案ID
     * @return 删除条
     */
    @Query("""
            DELETE FROM PolyModelPo u WHERE u.outputSchemaId = :outputSchemaId
            """)
    @Modifying
    int clearPolyModelByOutputSchemaId(@Param("outputSchemaId") Long outputSchemaId);

    /**
     * 查询输出方案绑定的全部聚合模型
     *
     * @param outputSchemaId 输出方案ID
     * @return 输出方案绑定的全部聚合模型
     */
    @Query("""
            SELECT u FROM PolyModelPo u WHERE u.outputSchemaId = :outputSchemaId ORDER BY u.seq ASC
            """)
    List<PolyModelPo> getPolyModelByOutputSchemaId(@Param("outputSchemaId") Long outputSchemaId);
}
