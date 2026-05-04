package com.ksptool.bio.biz.auth.repository;

import com.ksptool.bio.biz.auth.model.GroupPermissionPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermissionPo, GroupPermissionPo.Pk>, JpaSpecificationExecutor<GroupPermissionPo> {

    /**
     * 根据用户组ID获取拥有的全部权限ID
     *
     * @param gid 用户组ID
     * @return 权限ID列表
     */
    @Query("SELECT gp.permissionId FROM GroupPermissionPo gp WHERE gp.groupId = :gid")
    List<Long> getPidsByGid(@Param("gid") Long gid);
    
    /**
     * 根据用户组ID和权限ID列表删除权限关联
     *
     * @param gid 用户组ID
     * @param pids 权限ID列表
     * @return 删除的权限关联数量
     */
    @Modifying
    @Query("DELETE FROM GroupPermissionPo gp WHERE gp.groupId = :gid AND gp.permissionId IN :pids")
    int removeByGidAndPids(@Param("gid") Long gid, @Param("pids") List<Long> pids);

    /**
     * 根据用户组ID和权限ID获取权限关联
     *
     * @param groupId      用户组ID
     * @param permissionId 权限ID
     * @return 权限关联
     */
    @Query("""
            SELECT gp FROM GroupPermissionPo gp WHERE gp.groupId = :groupId AND gp.permissionId = :permissionId
            """)
    GroupPermissionPo getGpByGroupIdAndPermissionId(@Param("groupId") Long groupId, @Param("permissionId") Long permissionId);


    /**
     * 根据用户组ID获取拥有的全部权限ID
     *
     * @param groupId 用户组ID
     * @return 权限ID列表
     */
    @Query("""
            SELECT gp.permissionId FROM GroupPermissionPo gp
            WHERE gp.groupId = :groupId
            """)
    List<Long> getPermissionIdsByGroupId(@Param("groupId") Long groupId);


    /**
     * 清除用户组拥有的全部权限关联
     *
     * @param groupId 用户组ID
     */
    @Modifying
    @Query("""
            DELETE FROM GroupPermissionPo gp WHERE gp.groupId = :groupId
            """)
    void clearPermissionByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据权限码ID列表清除全部权限关联
     *
     * @param permissionIds 权限码ID列表 列表不能为空
     */
    @Modifying
    @Query("""
            DELETE FROM GroupPermissionPo gp WHERE gp.permissionId IN :permissionIds
            """)
    void clearGpByPermissionIds(@Param("permissionIds") List<Long> permissionIds);


    /**
     * 根据用户组ID列表清除权限关联
     *
     * @param groupIds 用户组ID列表 列表不能为空
     */
    @Modifying
    @Query("""
            DELETE FROM GroupPermissionPo gp WHERE gp.groupId IN :groupIds
            """)
    void clearPermissionByGroupIds(@Param("groupIds") List<Long> groupIds);


    /**
     * 根据权限ID统计有多少用户组在使用该权限
     *
     * @param permissionId 权限ID
     * @return 用户组数量
     */
    @Query("""
            SELECT COUNT(gp) FROM GroupPermissionPo gp WHERE gp.permissionId = :permissionId
            """)
    Long countGroupPermissionByPermissionId(@Param("permissionId") Long permissionId);

}
