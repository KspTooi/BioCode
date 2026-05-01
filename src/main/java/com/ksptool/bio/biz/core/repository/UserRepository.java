package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.auth.model.permission.PermissionPo;
import com.ksptool.bio.biz.core.model.user.UserPo;
import com.ksptool.bio.biz.core.model.user.dto.GetUserListDto;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserPo, Long> {

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户
     */
    @Query("""
            SELECT p FROM UserPo p
            WHERE p.username = :username
            """)
    UserPo getUserByUsername(@Param("username") String username);


    @Query("""
            SELECT
            u.id AS id,
            u.rootId AS rootId,
            r.name AS rootName,
            u.orgId AS orgId,
            c.name AS orgName,
            u.deptId AS deptId,
            d.name AS deptName,
            u.username AS username,
            u.nickname AS nickname,
            u.gender AS gender,
            u.phone AS phone,
            u.email AS email,
            u.createTime AS createTime,
            u.lastLoginTime AS lastLoginTime,
            u.status AS status,
            u.isSystem AS isSystem
            FROM UserPo u
            LEFT JOIN CoreRootPo r ON u.rootId = r.id
            LEFT JOIN OrgPo c ON u.orgId = c.id
            LEFT JOIN OrgPo d ON u.deptId = d.id
            WHERE (:#{#dto.username} IS NULL OR u.username LIKE CONCAT('%', :#{#dto.username}, '%'))
              AND (:#{#dto.nickname} IS NULL OR u.nickname LIKE CONCAT('%', :#{#dto.nickname}, '%'))
              AND (:#{#dto.phone} IS NULL OR u.phone LIKE CONCAT('%', :#{#dto.phone}, '%'))
              AND (:#{#dto.status} IS NULL OR u.status = :#{#dto.status})
              AND (:#{#dto.rootName} IS NULL OR r.name LIKE CONCAT('%', :#{#dto.rootName}, '%'))
              AND (
                    :#{#dto.orgId} IS NULL
                    OR c.id = :#{#dto.orgId}
                    OR d.id = :#{#dto.orgId}
                    OR CONCAT(',', d.orgPathIds, ',') LIKE CONCAT('%,', :#{#dto.orgId}, ',%')
                  )
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getUserList(@Param("dto") GetUserListDto dto, Pageable pageable);


    /**
     * 根据用户名统计用户数量(!!这会绕过软删除直接查询到被删除过的用户)
     *
     * @param username 用户名
     * @return 用户数量
     */
    @Query(
            value = """
                    SELECT COUNT(1)
                    FROM core_user
                    WHERE username = :username
                    """,
            nativeQuery = true
    )
    Integer countByUsername(@Param("username") String username);


    // 获取用户的所有权限（通过用户组）
    @Query("""
            SELECT DISTINCT p
            FROM UserGroupPo ug
            JOIN GroupPermissionPo gp ON ug.groupId = gp.groupId
            JOIN PermissionPo p ON gp.permissionId = p.id
            WHERE ug.userId = :userId
            """)
    List<PermissionPo> getUserPermissions(@Param("userId") Long userId);


    /**
     * 获取用户权限代码列表
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
    Set<String> getUserPermissionCodes(@Param("userId") Long userId);


    /**
     * 根据用户名列表查找用户
     *
     * @param usernames 用户名列表
     * @return 用户名列表
     */
    @Query("""
            SELECT DISTINCT p.username
            FROM UserPo p
            WHERE p.username IN :usernames
            """)
    Set<String> getUsernameSetByUsernames(@Param("usernames") List<String> usernames);


    /**
     * 获取用户ID列表
     *
     * @param pageable 分页信息
     * @return 用户ID列表
     */
    @Query("""
            SELECT p.id
            FROM UserPo p
            """)
    List<Long> getUserIdsList(Pageable pageable);

    /**
     * 统计用户数量
     */
    @Query("""
            SELECT COUNT(p.id) FROM UserPo p
            """)
    Long countUser();

    /**
     * 根据部门ID列表获取用户列表
     *
     * @param deptIds 部门ID列表
     * @return 用户列表
     */
    @Query("""
            SELECT p FROM UserPo p
            WHERE p.deptId IN :deptIds
            """)
    List<UserPo> getUserListByDeptIds(@Param("deptIds") List<Long> deptIds);


    /**
     * 统计系统内置用户数量
     *
     * @return 系统内置用户数量
     */
    @Query("""
            SELECT COUNT(p.id) FROM UserPo p WHERE p.isSystem = 1
            """)
    Integer countBySystemUser();

    /**
     * 根据用户ID获取数据版本
     *
     * @param userId 用户ID
     * @return 数据版本
     */
    @Query("""
            SELECT p.dataVersion FROM UserPo p
            WHERE p.id = :userId
            """)
    Long getDvByUserId(@Param("userId") Long userId);


    /**
     * 根据用户ID列表增加数据版本
     *
     * @param userIds 用户ID列表
     * @return 受影响的行数
     */
    @Modifying
    @Query("""
            UPDATE UserPo p SET p.dataVersion = p.dataVersion + 1 WHERE p.id IN :userIds
            """)
    int increaseDv(@Param("userIds") List<Long> userIds);


    /**
     * 根据企业/租户ID获取当前在线的用户ID列表
     *
     * @param rootId 企业/租户ID
     * @return 当前在线的用户ID列表
     */
    @Query("""
            SELECT DISTINCT p.id FROM UserPo p
            LEFT JOIN UserSessionPo usp ON p.id = usp.userId
            WHERE p.rootId = :rootId AND usp.expiresAt > NOW()
            """)
    List<Long> getOnlineUserIdsByRootId(@Param("rootId") Long rootId);

    /**
     * 根据权限ID获取当前在线的用户ID列表
     *
     * @param permissionId 权限ID
     * @return 当前在线的用户ID列表
     */
    @Query("""
            SELECT DISTINCT p.id FROM UserPo p
            JOIN UserSessionPo usp ON p.id = usp.userId
            JOIN UserGroupPo ug ON p.id = ug.userId
            JOIN GroupPermissionPo gp ON ug.groupId = gp.groupId
            WHERE gp.permissionId = :permissionId
            AND usp.expiresAt > NOW()
            """)
    List<Long> getOnlineUserIdsByPermissionId(@Param("permissionId") Long permissionId);

    @Query("""
                    SELECT u FROM UserPo u
                    LEFT JOIN OrgPo o ON u.rootId = o.id
                    WHERE u.id IN :memberIds AND u.rootId = :orgId
            """)
    List<UserPo> getUserByIdsAndOrgId(@Param("memberIds") List<Long> memberIds, @Param("orgId") Long orgId);

    @Query(value = """
                SELECT *
                FROM core_user
                WHERE id IN (:userIds)
            """, nativeQuery = true)
    List<UserPo> getUserListByUserIds(@Param("userIds") Set<Long> userIds);
}