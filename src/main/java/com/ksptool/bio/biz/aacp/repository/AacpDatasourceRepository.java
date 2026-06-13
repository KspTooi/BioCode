package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.datasource.AacpDatasourcePo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AacpDatasourceRepository extends JpaRepository<AacpDatasourcePo, Long> {

    /**
     * 根据编码统计数据源数量，排除指定ID（id为null时不排除）
     *
     * @param code 唯一编码
     * @param id   排除的ID，可为null
     * @return 数量
     */
    @Query("""
            SELECT COUNT(t) FROM AacpDatasourcePo t
            WHERE t.code = :code AND (:#{#id} IS NULL OR t.id != :id)
            """)
    Long countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    @Query("""
            SELECT u FROM AacpDatasourcePo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            ORDER BY u.createTime DESC
            """)
    Page<AacpDatasourcePo> getAacpDatasourceList(@Param("po") AacpDatasourcePo po, Pageable pageable);
}
