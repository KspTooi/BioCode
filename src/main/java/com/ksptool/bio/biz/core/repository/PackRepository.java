package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.pack.PackPo;
import com.ksptool.bio.biz.core.model.pack.dto.GetPackListDto;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface PackRepository extends JpaRepository<PackPo, Long>{

    /**
     * 查询菜单包列表
     *
     * @param dto      查询条件
     * @param pageable 分页条件
     * @return 菜单包列表
     */
    @Query("""
            SELECT
            p.id AS id,
            p.name AS name,
            p.code AS code,
            p.status AS status,
            p.seq AS seq,
            p.createTime AS createTime,
            (SELECT COUNT(mp) FROM MenuPackPo mp WHERE mp.packId = p.id) AS mCount
            FROM PackPo p
            WHERE
            (:#{#dto.name} IS NULL OR p.name LIKE CONCAT('%', :#{#dto.name}, '%'))
            AND (:#{#dto.code} IS NULL OR p.code LIKE CONCAT('%', :#{#dto.code}, '%'))
            AND (:#{#dto.status} IS NULL OR p.status = :#{#dto.status} )
            ORDER BY p.seq ASC, p.createTime ASC
            """)
    Page<Tuple> getPackList(@Param("dto") GetPackListDto dto, Pageable pageable);

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

    /**
     * 根据ID列表统计菜单包数量
     *
     * @param ids ID列表
     * @return 存在的数量
     */
    @Query("""
            SELECT COUNT(p) FROM PackPo p WHERE p.id IN :ids
            """)
    long countByIds(@Param("ids") List<Long> ids);
}
