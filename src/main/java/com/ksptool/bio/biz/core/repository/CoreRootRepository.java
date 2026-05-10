package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.root.CoreRootPo;
import com.ksptool.bio.biz.core.model.root.dto.GetCoreRootListDto;
import jakarta.persistence.Tuple;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            u.isSystem AS isSystem,
            up.username AS adminUsername,
            (SELECT COUNT(usr) FROM UserPo usr WHERE usr.rootId = u.id) AS ruCount,
            u.createTime AS createTime
            FROM CoreRootPo u
            LEFT JOIN UserPo up ON u.adminUserId = up.id
            WHERE
            (:#{#dto.name} IS NULL OR u.name LIKE CONCAT('%', :#{#dto.name}, '%'))
            AND (:#{#dto.expireTimeRangeStart} IS NULL OR u.expireTime >= :#{#dto.expireTimeRangeStart})
            AND (:#{#dto.expireTimeRangeEnd} IS NULL OR u.expireTime <= :#{#dto.expireTimeRangeEnd})
            AND (:#{#dto.status} IS NULL OR u.status = :#{#dto.status} )
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getCoreRootList(@Param("dto") GetCoreRootListDto dto, Pageable pageable);

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


    /**
     * 判断指定用户在指定租户下是否为租管
     */
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM CoreRootPo r
        WHERE r.id = :rid
          AND r.adminGroupId IN (
              SELECT ug.groupId FROM UserGroupPo ug
              WHERE ug.userId = :uid
          )
        """)
    boolean isAdminOfRoot(@Param("rid") Long rid, @Param("uid") Long uid);

}
