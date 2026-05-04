package com.ksptool.bio.biz.auth.repository;

import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import com.ksptool.bio.biz.auth.model.permission.dto.GetPermissionListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 权限数据访问接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionPo, Long> {

    /**
     * 根据权限代码获取权限
     *
     * @param code 权限代码
     * @return 权限
     */
    @Query("""
            SELECT p FROM PermissionPo p WHERE p.code = :code
            """)
     PermissionPo getByCode(@Param("code") String code);


    /**
     * 根据用户ID获取用户拥有的全部权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query("""
            SELECT DISTINCT p
            FROM UserGroupPo ug
            JOIN GroupPermissionPo gp ON ug.groupId = gp.groupId
            JOIN PermissionPo p ON gp.permissionId = p.id
            WHERE ug.userId = :userId
            """)
    List<PermissionPo> getPermissionsByUserId(@Param("userId") Long userId);


    /**
     * 获取全部系统权限码
     *
     * @return 系统权限码列表
     */
    @Query("""
            SELECT DISTINCT p FROM PermissionPo p WHERE p.isSystem = 1
            """)
    Set<PermissionPo> getAllSystemPermissions();


    /**
     * 从给定的权限列表中查找数据库中存在的权限
     *
     * @param codes 给定的权限代码列表
     * @return 数据库中存在的权限代码列表
     */
    @Query("""
            SELECT p.code
            FROM PermissionPo p
            WHERE p.code IN :codes
            """)
    Set<String> getExistingPermissionsByCode(@Param("codes") Set<String> codes);


    /**
     * 根据权限代码列表获取权限列表
     *
     * @param codes 权限代码列表
     * @return 权限列表
     */
    @Query("""
            SELECT DISTINCT p
            FROM PermissionPo p
            WHERE p.code IN :codes
            """)
    Set<PermissionPo> getPermissionsByCodes(@Param("codes") Set<String> codes);


    /**
     * 根据关键词和用户组ID获取权限列表
     *
     * @param keyword       关键词
     * @param groupId       用户组ID
     * @param hasPermission 是否拥有权限
     * @param pageable      分页信息
     * @return 权限列表
     * 如果hasPermission为1，则查询拥有该权限的权限列表
     * 如果hasPermission为0，则查询未拥有该权限的权限列表
     * 如果hasPermission为null，则查询全部权限列表
     */
    @Query("""
            SELECT DISTINCT p
            FROM PermissionPo p
            WHERE
            ( (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR
            (:keyword IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) )
            AND (:groupId IS NULL OR :hasPermission IS NULL OR
                 ((:hasPermission = 1 AND EXISTS (SELECT 1 FROM GroupPermissionPo gp WHERE gp.groupId = :groupId))
                  OR (:hasPermission = 0 AND NOT EXISTS (SELECT 1 FROM GroupPermissionPo gp WHERE gp.groupId = :groupId))))
            ORDER BY p.seq ASC
            """)
    Page<PermissionPo> getPermissionsByKeywordAndGroup(@Param("keyword") String keyword, @Param("groupId") Long groupId, @Param("hasPermission") Integer hasPermission, Pageable pageable);


    /**
     * 根据关键词获取权限列表
     *
     * @param dto      查询条件
     * @param pageable 分页信息
     * @return 权限列表
     */
    @Query("""
            SELECT p
            FROM PermissionPo p
            WHERE (:#{#dto.code} IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :#{#dto.code}, '%')))
            AND (:#{#dto.name} IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#dto.name}, '%')))
            ORDER BY p.seq ASC
            """)
    Page<PermissionPo> getPermissionList(
            @Param("dto") GetPermissionListDto dto,
            Pageable pageable);


    /**
     * 根据用户组ID获取权限列表
     *
     * @param groupId 用户组ID
     * @return 权限列表
     */
    @Query("""
            SELECT p FROM PermissionPo p
            LEFT JOIN GroupPermissionPo gp ON p.id = gp.permissionId
            WHERE gp.groupId = :groupId
            """)
    List<PermissionPo> getPermissionsByGroupId(@Param("groupId") Long groupId);

     /**
     * 根据用户ID获取用户拥有的全部权限代码
     *
     * @param userId 用户ID
     * @return 权限代码列表
     */
    @Query("""
            SELECT DISTINCT p.code
            FROM UserGroupPo ug
            JOIN GroupPermissionPo gp ON ug.groupId = gp.groupId
            JOIN PermissionPo p ON gp.permissionId = p.id
            WHERE ug.userId = :userId
            """)
    Set<String> getCodesByUserId(@Param("userId") Long userId);

} 