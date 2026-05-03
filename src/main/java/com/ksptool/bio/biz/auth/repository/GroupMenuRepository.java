package com.ksptool.bio.biz.auth.repository;

import com.ksptool.bio.biz.auth.model.GroupMenuPo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMenuRepository extends JpaRepository<GroupMenuPo, GroupMenuPo.Pk> {

    /**
     * 根据用户组ID获取拥有的全部菜单ID列表
     *
     * @param gid 用户组ID
     * @return 菜单ID列表
     */
    @Query("SELECT gm.menuId FROM GroupMenuPo gm WHERE gm.groupId = :gid")
    List<Long> getMidsByGid(@Param("gid") Long gid);

    /**
     * 根据用户组ID和菜单ID列表删除菜单关联
     *
     * @param gid 用户组ID
     * @param mids 菜单ID列表
     * @return 删除的菜单关联数量
     */
    @Modifying
    @Query("DELETE FROM GroupMenuPo gm WHERE gm.groupId = :gid AND gm.menuId IN :mids")
    int removeByGidAndMids(@Param("gid") Long gid, @Param("mids") List<Long> mids);
    
}
