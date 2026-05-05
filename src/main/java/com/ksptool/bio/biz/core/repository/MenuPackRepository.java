package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.pack.MenuPackPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuPackRepository extends JpaRepository<MenuPackPo, MenuPackPo.Pk> {

    /**
     * 根据菜单包ID获取拥有的全部菜单ID列表
     *
     * @param pid 菜单包ID
     * @return 菜单ID列表
     */
    @Query("SELECT mp.menuId FROM MenuPackPo mp WHERE mp.packId = :pid")
    List<Long> getMidsByPid(@Param("pid") Long pid);

    /**
     * 根据菜单包ID和菜单ID列表删除菜单关联
     *
     * @param pid  菜单包ID
     * @param mids 菜单ID列表
     * @return 删除的菜单关联数量
     */
    @Modifying
    @Query("DELETE FROM MenuPackPo mp WHERE mp.packId = :pid AND mp.menuId IN :mids")
    int removeByPidAndMids(@Param("pid") Long pid, @Param("mids") List<Long> mids);

    /**
     * 根据菜单ID删除菜单关联
     *
     * @param mid 菜单ID
     * @return 删除的菜单关联数量
     */
    @Modifying
    @Query("DELETE FROM MenuPackPo mp WHERE mp.menuId = :mid")
    int removeByMid(@Param("mid") Long mid);

    /**
     * 根据菜单包ID删除菜单关联
     *
     * @param pid 菜单包ID
     * @return 删除的菜单关联数量
     */
    @Modifying
    @Query("DELETE FROM MenuPackPo mp WHERE mp.packId = :pid")
    int removeByPid(@Param("pid") Long pid);

    /**
     * 根据菜单ID获取拥有的全部菜单包ID列表
     *
     * @param mid 菜单ID
     * @return 菜单包ID列表
     */
    @Query("SELECT mp.packId FROM MenuPackPo mp WHERE mp.menuId = :mid")
    List<Long> getPidsByMid(@Param("mid") Long mid);
}
