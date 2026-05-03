package com.ksptool.bio.biz.auth.repository;

import com.ksptool.bio.biz.auth.model.GroupDeptPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupDeptRepository extends JpaRepository<GroupDeptPo, GroupDeptPo.Pk> {

    /**
     * 根据用户组ID清除部门关系
     *
     * @param groupId 用户组ID
     */
    @Modifying
    @Query("DELETE FROM GroupDeptPo u WHERE u.groupId = :groupId")
    int removeByGid(@Param("groupId") Long groupId);

    /**
     * 根据用户组ID获取与之关联的部门ID列表
     *
     * @param gid 用户组ID
     * @return 与之关联的部门ID列表
     */
    @Query("SELECT u.deptId FROM GroupDeptPo u WHERE u.groupId = :gid")
    List<Long> getDidsByGid(@Param("gid") Long gid);

    /**
     * 根据用户组ID和部门ID列表删除部门关联
     *
     * @param gid 用户组ID
     * @param dids 部门ID列表
     * @return 删除的部门关联数量
     */
    @Modifying
    @Query("DELETE FROM GroupDeptPo u WHERE u.groupId = :gid AND u.deptId IN :dids")
    int removeByGidAndDids(@Param("gid") Long gid, @Param("dids") List<Long> dids);

    /**
     * 根据用户组ID清除部门关系
     *
     * @param groupId 用户组ID
     */
    @Modifying
    @Query("""
            DELETE FROM GroupDeptPo u WHERE u.groupId = :groupId
            """)
    Integer clearGroupDeptByGroupId(@Param("groupId") Long groupId);

    /**
     * 根据用户组ID获取部门ID列表
     *
     * @param groupId 用户组ID
     * @return 部门ID列表
     */
    @Query("""
            SELECT u.deptId FROM GroupDeptPo u WHERE u.groupId = :groupId
            """)
    List<Long> getDeptIdsByGroupId(@Param("groupId") Long groupId);


    /**
     * 根据组IDS获取与之关联的部门IDS
     *
     * @param groupIds 组ID列表
     * @return 与之关联的部门IDS
     */
    @Query("""
            SELECT DISTINCT u.deptId FROM GroupDeptPo u WHERE u.groupId IN :groupIds
            """)
    List<Long> getDeptIdsByGroupIds(@Param("groupIds") List<Long> groupIds);

}
