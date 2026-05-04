package com.ksptool.bio.biz.auth.repository;


import com.ksptool.bio.biz.auth.model.group.GroupPo;
import com.ksptool.bio.biz.auth.model.group.dto.GetGroupListDto;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupPo, Long>, JpaSpecificationExecutor<GroupPo> {


    /**
     * 根据用户ID获取用户拥有的全部用户组
     *
     * @param userId 用户ID
     * @return 用户拥有的全部用户组
     */
    @Query("""
            SELECT g FROM GroupPo g
            LEFT JOIN UserGroupPo ug ON g.id = ug.groupId
            WHERE ug.userId = :userId AND g.status = :status
            """)
    List<GroupPo> getGroupsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 根据标识统计用户组数量 排除指定ID
     *
     * @param code 用户组标识
     * @param id   需排除的ID
     * @return 用户组数量
     */
    @Query("""
            SELECT COUNT(g) FROM GroupPo g WHERE g.code = :code AND (:id IS NULL OR g.id != :id)
            """)
    int countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);

    /**
     * 根据ID列表获取用户组及其关联的用户和权限
     *
     * @param ids 用户组ID列表
     * @return 用户组及其关联的用户和权限
     */
    @Query("""
            SELECT g FROM GroupPo g
            WHERE g.id IN :ids
            """)
    List<GroupPo> getGroupsByIds(@Param("ids") List<Long> ids);

    /**
     * 获取最大排序号
     *
     * @return 最大排序号，如果没有记录则返回0
     */
    @Query("""
            SELECT COALESCE(MAX(g.seq), 0)
            FROM GroupPo g
            """)
    Integer findMaxSortOrder();

    @Query("""
            SELECT
            g.id AS id,
            g.code AS code,
            g.name AS name,
            (SELECT COUNT(ug) FROM UserGroupPo ug WHERE ug.groupId = g.id) AS guCount,
            (SELECT COUNT(gm) FROM GroupMenuPo gm WHERE gm.groupId = g.id) AS gmCount,
            (SELECT COUNT(gp) FROM GroupPermissionPo gp WHERE gp.groupId = g.id) AS gpCount,
            g.rowScope AS rowScope,
            g.isSystem AS isSystem,
            g.status AS status,
            g.seq AS seq,
            g.createTime AS createTime
            FROM GroupPo g
            WHERE (:#{#dto.keyword} IS NULL OR g.code LIKE %:#{#dto.keyword}%
                OR g.name LIKE %:#{#dto.keyword}%
                OR g.remark LIKE %:#{#dto.keyword}%)
            AND (:#{#dto.status} IS NULL OR g.status = :#{#dto.status})
            ORDER BY g.seq ASC, g.id DESC
            """)
    Page<Tuple> getGroupList(@Param("dto") GetGroupListDto dto, Pageable pageable);


    /**
     * 根据组码获取用户组
     *
     * @param code 组码
     * @return 用户组
     */
    @Query("""
            SELECT g FROM GroupPo g WHERE g.code = :code
            """)
    GroupPo getGroupByCode(@Param("code") String code);

    /**
     * 统计系统内置组数量
     *
     * @return 系统内置组数量
     */
    @Query("""
            SELECT COUNT(g) FROM GroupPo g WHERE g.isSystem = 1
            """)
    Integer countBySystemGroup();

    /**
     * 根据ID列表获取用户组
     *
     * @param ids    用户组ID列表
     * @param status 用户组状态 0:禁用 1:启用
     * @return 用户组
     */
    @Query("""
            SELECT g.id FROM GroupPo g
            WHERE g.id IN :ids AND g.status = :status
            """)
    List<Long> getUserGroupByIds(@Param("ids") List<Long> ids, Integer status);
}