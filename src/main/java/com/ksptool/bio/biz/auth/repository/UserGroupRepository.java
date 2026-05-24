package com.ksptool.bio.biz.auth.repository;

import com.ksptool.bio.biz.auth.model.UserGroupPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroupPo, UserGroupPo.Pk>, JpaSpecificationExecutor<UserGroupPo> {


    /**
     * 批量查询用户组下的用户ID
     *
     * @param groupIds 用户组ID列表
     * @return 用户组关联实体列表
     */
    @Query("""
            SELECT ug FROM UserGroupPo ug
            WHERE ug.groupId IN :groupIds
            """)
    List<UserGroupPo> findAllByGroupIds(@Param("groupIds") List<Long> groupIds);

    /**
     * 根据用户组ID统计用户数量
     *
     * @param groupId 用户组ID
     * @return 用户数量
     */
    @Query("""
            SELECT COUNT(ug) FROM UserGroupPo ug WHERE ug.groupId = :groupId
            """)
    Long countUserByGroupId(@Param("groupId") Long groupId);


    /**
     * 根据用户ID获取用户拥有的全部用户组ID
     *
     * @param userId 用户ID
     * @return 用户拥有的全部用户组ID
     */
    @Query("""
            SELECT ug.groupId FROM UserGroupPo ug
            WHERE ug.userId = :userId
            """)
    List<Long> getGroupIdsByGrantedUserId(@Param("userId") Long userId);

    /**
     * 清除用户拥有的全部用户组关联
     *
     * @param userId 用户ID
     * @return 清除的用户组关联数量
     */
    @Modifying
    @Query("""
            DELETE FROM UserGroupPo ug WHERE ug.userId = :userId
            """)
    void clearGroupGrantedByUserId(@Param("userId") Long userId);


    /**
     * 根据用户组ID获取拥有该组的用户ID列表
     *
     * @param groupId 用户组ID
     * @return 用户ID列表
     */
    @Query("""
            SELECT ug.userId FROM UserGroupPo ug
            WHERE ug.groupId = :groupId
            """)
    List<Long> getUserIdsByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据用户ID和用户组ID获取用户组关联
     *
     * @param userId  用户ID
     * @param groupId 用户组ID
     * @return 用户组关联
     */
    @Query("""
                    SELECT ug FROM UserGroupPo ug WHERE ug.userId = :userId AND ug.groupId = :groupId
            """)
    UserGroupPo getUgByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);

}
