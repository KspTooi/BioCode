package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.rawmodel.RawModelPo;
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
public interface RawModelRepository extends JpaRepository<RawModelPo, Long> {

    /**
     * 查询输出方案原始字段
     *
     * @param outputSchemaId 输出方案ID
     * @return 输出方案原始字段
     */
    @Query("""
            SELECT u FROM RawModelPo u
            WHERE u.outputSchemaId = :outputSchemaId
            ORDER BY u.seq ASC
            """)
    List<RawModelPo> getRawModelByOutputSchemaId(Long outputSchemaId);


    /**
     * 查询输出方案原始字段列表
     *
     * @param po 查询参数
     * @return 输出方案原始字段列表
     */
    @Query("""
            SELECT u FROM RawModelPo u
            WHERE
            (:#{#po.outputSchemaId} IS NULL OR u.outputSchemaId  = :#{#po.outputSchemaId} )
            ORDER BY u.seq ASC
            """)
    List<RawModelPo> getRawModelList(@Param("po") RawModelPo po);

    /**
     * 清空输出方案原始模型
     *
     * @param outputSchemaId 输出方案ID
     * @return 删除条
     */
    @Query("""
            DELETE FROM RawModelPo u WHERE u.outputSchemaId = :outputSchemaId
            """)
    @Modifying
    void clearRawModelByOutputSchemaId(@Param("outputSchemaId") Long outputSchemaId);
}
