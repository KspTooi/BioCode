package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.pack.PackPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PackRepository extends JpaRepository<PackPo, Long>{

    /**
     * 查询菜单包列表
     *
     * @param po       查询条件
     * @param pageable 分页条件
     * @return 菜单包列表
     */
    @Query("""
            SELECT u FROM PackPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<PackPo> getPackList(@Param("po") PackPo po, Pageable pageable);

    /**
     * 根据编码统计菜单包数量
     *
     * @param code 编码
     * @return 菜单包数量
     */
    @Query("""
            SELECT COUNT(u) FROM PackPo u WHERE u.code = :code
            """)
    Integer countByCode(@Param("code") String code);
}
