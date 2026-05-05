package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.menu.MenuPo;
import com.ksptool.bio.biz.core.model.menu.dto.GetMenuTreeDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<MenuPo, Long> {


    /**
     * 获取菜单与按钮树
     *
     * @return 菜单与按钮树
     */
    @Query("""
            SELECT t FROM MenuPo t
            WHERE t.hide = 0
            ORDER BY t.seq ASC,t.createTime DESC
            """)
    List<MenuPo> getUserMenuTree();

    /**
     * 获取菜单与按钮树
     *
     * @param po 获取菜单与按钮树参数
     * @return 菜单与按钮树
     */
    @Query("""
            SELECT t FROM MenuPo t
            WHERE (:#{#po.kind} IS NULL OR t.kind = :#{#po.kind})
            AND (
                :#{#po.name} IS NULL OR (t.name LIKE CONCAT('%',:#{#po.name},'%') OR
                t.remark LIKE CONCAT('%',:#{#po.name},'%') )
            )
            AND (:#{#po.permissionCode} IS NULL OR t.permissionCode LIKE CONCAT('%',:#{#po.permissionCode},'%') )
            ORDER BY t.seq ASC,t.createTime DESC
            """)
    List<MenuPo> getMenuTree(@Param("po") GetMenuTreeDto po);

    /**
     * 按关键字查询菜单列表（用于权限分配视图）
     *
     * @param keyword 关键字，匹配名称或路径，为null时查全部
     * @return 菜单列表
     */
    @Query("""
            SELECT t FROM MenuPo t
            WHERE (:keyword IS NULL OR t.name LIKE CONCAT('%',:keyword,'%')
                   OR t.path LIKE CONCAT('%',:keyword,'%'))
            ORDER BY t.seq ASC, t.createTime DESC
            """)
    List<MenuPo> getMenusByKeyword(@Param("keyword") String keyword);

    /**
     * 获取菜单子级数量
     *
     * @param id 菜单ID
     * @return 菜单子级数量
     */
    @Query("""
            SELECT COUNT(t) FROM MenuPo t
            WHERE t.parentId = :id
            """)
    int getMenuChildrenCount(@Param("id") Long id);

    /**
     * 清空菜单
     */
    @Query("""
            DELETE FROM MenuPo t
            """)
    @Modifying
    void clearMenu();

    /**
     * 获取可授予菜单列表
     *
     * @param rid 租户ID
     * @param uid 用户ID
     * @return 可授予菜单列表
     */
    @Query("""
            SELECT t FROM MenuPo t
            WHERE t.id IN (
                SELECT gm.menuId FROM GroupMenuPo gm
                WHERE gm.groupId IN (
                    SELECT ug.groupId FROM UserGroupPo ug
                    WHERE ug.userId = :uid
                )
            )
            AND t.hide = 0 AND t.rootId = :rid
            ORDER BY t.seq ASC,t.createTime DESC
            """)
    List<MenuPo> getGrantedMenus(@Param("rid") Long rid,@Param("uid") Long uid);
}
