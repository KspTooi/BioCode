package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.AacpCapDatasourcePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapDatasourceRepository extends JpaRepository<AacpCapDatasourcePo, AacpCapDatasourcePo.Pk> {

    /**
     * 根据数据源ID统计能力包数量
     */
    @Query("""
            SELECT COUNT(u) FROM AacpCapDatasourcePo u WHERE u.datasourceId = :datasourceId
            """)
    long countByDatasourceId(@Param("datasourceId") Long datasourceId);

    /**
     * 根据能力包ID获取已绑定的数据源ID列表
     */
    @Query("""
            SELECT u.datasourceId FROM AacpCapDatasourcePo u WHERE u.capId = :capId
            """)
    List<Long> getDatasourceIdsByCapId(@Param("capId") Long capId);

    /**
     * 根据能力包ID和数据源ID列表删除关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpCapDatasourcePo u WHERE u.capId = :capId AND u.datasourceId IN :datasourceIds
            """)
    void removeByCapIdAndDatasourceIds(@Param("capId") Long capId, @Param("datasourceIds") List<Long> datasourceIds);

    /**
     * 根据能力包ID删除所有关联
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM AacpCapDatasourcePo u WHERE u.capId = :capId
            """)
    void removeByCapId(@Param("capId") Long capId);
}
