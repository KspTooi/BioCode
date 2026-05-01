package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.root.CoreRootPo;

import jakarta.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoreRootRepository extends JpaRepository<CoreRootPo, Long> {

    @Query("""
            SELECT 
            u.id AS id,
            u.name AS name,
            u.expireTime AS expireTime,
            u.status AS status,
            up.username AS adminUsername,
            u.createTime AS createTime
            FROM CoreRootPo u
            LEFT JOIN UserPo up ON u.adminUserId = up.id
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.expireTime} IS NULL OR u.expireTime = :#{#po.expireTime} )
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getCoreRootList(@Param("po") CoreRootPo po, Pageable pageable);
    
    /**
     * 根据名称统计租户数量 排除指定ID
     *
     * @param name 租户名称
     * @param id   需排除的ID
     * @return 租户数量
     */
    @Query("""
            SELECT COUNT(u) FROM CoreRootPo u WHERE u.name = :name AND (:id IS NULL OR u.id != :id)
            """)
    int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);

}
