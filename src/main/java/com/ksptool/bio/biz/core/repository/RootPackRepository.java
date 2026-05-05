package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.pack.RootPackPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RootPackRepository extends JpaRepository<RootPackPo, RootPackPo.Pk> {

    /**
     * 根据租户ID获取绑定的全部菜单包ID列表
     *
     * @param rid 租户ID
     * @return 菜单包ID列表
     */
    @Query("SELECT rp.packId FROM RootPackPo rp WHERE rp.rootId = :rid")
    List<Long> getPidsByRid(@Param("rid") Long rid);

    /**
     * 根据租户ID和菜单包ID列表删除关联
     *
     * @param rid  租户ID
     * @param pids 菜单包ID列表
     * @return 删除的关联数量
     */
    @Modifying
    @Query("DELETE FROM RootPackPo rp WHERE rp.rootId = :rid AND rp.packId IN :pids")
    int removeByRidAndPids(@Param("rid") Long rid, @Param("pids") List<Long> pids);

    /**
     * 根据菜单包ID删除所有关联
     *
     * @param pid 菜单包ID
     * @return 删除的关联数量
     */
    @Modifying
    @Query("DELETE FROM RootPackPo rp WHERE rp.packId = :pid")
    int removeByPid(@Param("pid") Long pid);

    /**
     * 根据租户ID删除所有关联
     *
     * @param rid 租户ID
     * @return 删除的关联数量
     */
    @Modifying
    @Query("DELETE FROM RootPackPo rp WHERE rp.rootId = :rid")
    int removeByRid(@Param("rid") Long rid);
}
