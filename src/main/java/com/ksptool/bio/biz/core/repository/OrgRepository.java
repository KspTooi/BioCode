package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.org.OrgPo;
import com.ksptool.bio.biz.core.model.org.dto.GetOrgListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 组织机构仓库
 * kind: 0:企业(租户) 1:子企业 2:部门 3:班组
 */
@Repository
public interface OrgRepository extends JpaRepository<OrgPo, Long> {


    /**
     * 根据名称和级别查询组织机构数量
     *
     * @param name  组织机构名称
     * @param level 组织机构级别
     * @return 组织机构数量
     */
    @Query("SELECT COUNT(u) FROM OrgPo u WHERE u.name = :name AND u.level = :level")
    int countByNameAndLevel(@Param("name") String name, @Param("level") Integer level);

    /**
     * 根据名称和上级组织ID查询组织机构数量 排除指定ID
     *
     * @param name     组织机构名称
     * @param parentId 上级组织ID
     * @param id       需排除的ID
     * @return 组织机构数量
     */
    @Query("SELECT COUNT(u) FROM OrgPo u WHERE u.name = :name AND (u.parentId = :parentId OR (:parentId IS NULL AND u.parentId IS NULL)) AND (:id IS NULL OR u.id != :id)")
    int countByNameAndParentIdExcludeId(@Param("name") String name, @Param("parentId") Long parentId, @Param("id") Long id);

    /**
     * 根据名称查询企业(租户)
     *
     * @param name 企业(租户)名称
     * @return 企业(租户)
     */
    @Query("SELECT d FROM OrgPo d WHERE d.name = :name AND d.kind = 0")
    OrgPo getRootByName(@Param("name") String name);

    /**
     * 根据id列表查询企业(租户)
     *
     * @param ids 企业id列表
     * @return 企业列表
     */
    @Query("SELECT d FROM OrgPo d WHERE d.id IN :ids AND d.kind = 0")
    List<OrgPo> getRootsByIds(@Param("ids") List<Long> ids);


    /**
     * 根据id查询企业
     *
     * @param id 企业id
     * @return 企业
     */
    @Query("SELECT d FROM OrgPo d WHERE d.id = :id AND d.kind = 0")
    OrgPo getRootById(@Param("id") Long id);

    /**
     * 根据部门id查询部门
     *
     * @param deptId 部门id
     * @return 部门
     */
    @Query("SELECT d FROM OrgPo d WHERE d.id = :deptId AND d.kind = 2")
    OrgPo getDeptById(@Param("deptId") Long deptId);

    /**
     * 根据部门id列表查询多个部门
     *
     * @param deptIds 部门id列表
     * @return 部门列表
     */
    @Query("SELECT d FROM OrgPo d WHERE d.id IN :deptIds AND d.kind = 2")
    List<OrgPo> getDeptsByIds(@Param("deptIds") List<Long> deptIds);

    /**
     * 根据部门名称查询部门
     *
     * @param name  部门名称
     * @param orgId 组织id
     * @return 部门
     */
    @Query("SELECT d FROM OrgPo d WHERE d.name = :name AND d.kind = 2 AND d.rootId = :orgId")
    OrgPo getDeptByName(@Param("name") String name, @Param("orgId") Long orgId);


    @Query("""
            SELECT u FROM OrgPo u
            WHERE
           
             (:#{#po.orgIds} IS NULL OR u.id in  :#{#po.orgIds} )            
            ORDER BY u.updateTime DESC
            """)
    Page<OrgPo> getOrgList(@Param("po") GetOrgListDto po, Pageable pageable);


    /**
     * 根据一级组织ID查询组织机构数量
     *
     * @param parentId 一级组织ID
     * @return 组织机构数量
     */
    @Query("""
            SELECT COUNT(u) FROM OrgPo u
            WHERE u.parentId = :parentId
            """)
    Integer countByParentId(@Param("parentId") Long parentId);


    /**
     * 根据部门id列表查询用户数量
     *
     * @param ids 部门id列表
     * @return 用户数量
     */
    @Query("""
            SELECT COUNT(u) FROM UserPo u
            WHERE u.deptId IN :ids
            """)
    Integer countUserByDeptIds(@Param("ids") List<Long> ids);

    /**
     * 根据名称查询企业数量
     *
     * @param name 企业名称
     * @return 企业数量
     */
    @Query("""
            SELECT COUNT(u) FROM OrgPo u WHERE u.name = :name AND u.kind = 0
            """)
    Long countRootByName(@Param("name") String name);

    /**
     * 根据名称查询企业数量 排除指定ID
     *
     * @param name 企业名称
     * @param id   需排除的ID
     * @return 企业数量
     */
    @Query("""
            SELECT COUNT(u) FROM OrgPo u WHERE u.name = :name AND u.kind = 0 AND u.id != :id
            """)
    Long countRootByNameExcludeId(@Param("name") String name, @Param("id") Long id);

    /**
     * 根据名称和上级部门ID查询部门数量
     *
     * @param name     部门名称
     * @param parentId 上级部门ID
     * @return 部门数量
     */
    @Query("""
            SELECT COUNT(u) FROM OrgPo u WHERE u.name = :name AND u.kind > 0 AND u.parentId = :parentId
            """)
    Long countDeptByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);


    /**
     * 根据名称和上级部门ID查询部门数量 排除指定ID
     *
     * @param name     部门名称
     * @param parentId 上级部门ID
     * @param id       需排除的ID
     * @return 部门数量
     */
    @Query("""
            SELECT COUNT(u) FROM OrgPo u WHERE u.name = :name AND u.kind > 0 AND u.parentId = :parentId AND u.id != :id
            """)
    Long countDeptByNameAndParentIdExcludeId(@Param("name") String name, @Param("parentId") Long parentId, @Param("id") Long id);

    /**
     * 根据部门id查询部门以及其子部门
     *
     * @param deptId 部门id
     * @return 部门以及其子部门
     */
    @Query("""
            SELECT d FROM OrgPo d
            WHERE d.kind > 0
            AND (
                d.id = :deptId
                OR CONCAT(',', d.orgPathIds, ',') LIKE CONCAT('%,', :deptId, ',%')
            )
            """)
    List<OrgPo> getChildDeptsByDeptId(@Param("deptId") Long deptId);


    /**
     * 根据组织ID查询组织以及其子组织
     *
     * @param orgId 组织ID
     * @return 组织以及其子组织
     */
    @Query("""
            SELECT d FROM OrgPo d
            WHERE (
                d.id = :orgId
                OR CONCAT(',', d.orgPathIds, ',') LIKE CONCAT('%,', :orgId, ',%')
            )
            """)
    List<OrgPo> getChildByOrgId(@Param("orgId") Long orgId);

    /**
     * RS=20 仅本公司：当前公司 + 当前公司直属的所有非企业组织(部门或者班组或者其他机构)
     *
     * @param orgId 组织ID
     * @return 组织列表
     */
    @Query("""
            SELECT o FROM OrgPo o
            WHERE o.id = :orgId OR (o.orgId = :orgId AND o.kind > 1)
            """)
    List<OrgPo> getRowScope20OrgScopeListByOrgId(@Param("orgId") Long orgId);

    /**
     * 根据父级ID查询组织
     *
     * @param parentId 父级ID
     * @return 组织
     */
    @Query("""
            SELECT d FROM OrgPo d
            WHERE d.parentId = :parentId
            ORDER BY d.seq ASC
            """)
    List<OrgPo> getByParentId(@Param("parentId") Long parentId);

    /**
     * 根据ID查询组织
     *
     * @param ids ID
     * @return 组织
     */
    @Query("""
            SELECT d FROM OrgPo d
            WHERE d.id in :ids
            """)
    List<OrgPo> getByIds(@Param("ids") List<Long> ids);


    @Query("""
            SELECT d FROM OrgPo d
            WHERE d.rootId = :rootId
            order by d.seq asc
            """)
    List<OrgPo> getAllByRootId(@Param("rootId") Long rootId);

    @Query("""
            SELECT d FROM OrgPo d
            WHERE d.orgId = :orgId
            order by d.seq asc
            """)
    List<OrgPo> getAllByOrgId(@Param("orgId") Long orgId);

}
