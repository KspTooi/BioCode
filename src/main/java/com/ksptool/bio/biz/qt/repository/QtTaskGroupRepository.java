package com.ksptool.bio.biz.qt.repository;

import com.ksptool.bio.biz.qt.model.qttaskgroup.QtTaskGroupPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * QF任务分组Repository
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-02-10
 * @license Apache License 2.0
 */
@Repository
public interface QtTaskGroupRepository extends JpaRepository<QtTaskGroupPo, Long> {

    @Query("""
            SELECT u FROM QtTaskGroupPo u
            WHERE
            (:#{#po.id} IS NULL OR u.id  = :#{#po.id} )
            AND (:#{#po.name} IS NULL OR u.name  LIKE CONCAT('%', :#{#po.name}, '%') )
            AND (:#{#po.remark} IS NULL OR u.remark  LIKE CONCAT('%', :#{#po.remark}, '%') )
            ORDER BY u.createTime DESC
            """)
    Page<QtTaskGroupPo> getQtTaskGroupList(@Param("po") QtTaskGroupPo po, Pageable pageable);


    /**
     * 根据名称统计任务分组数量
     *
     * @param name 任务分组名称
     * @return 任务分组数量
     */
    @Query("""
            SELECT COUNT(t) FROM QtTaskGroupPo t
            WHERE t.name = :name
            """)
    Long countByName(@Param("name") String name);

    /**
     * 根据名称统计任务分组数量 排除指定ID
     *
     * @param name 任务分组名称
     * @param id   任务分组ID
     * @return 任务分组数量
     */
    @Query("""
            SELECT COUNT(t) FROM QtTaskGroupPo t
            WHERE t.name = :name AND t.id != :id
            """)
    Long countByNameExcludeId(@Param("name") String name, @Param("id") Long id);


    /**
     * 根据名称获取任务分组
     *
     * @param name 任务分组名称
     * @return 任务分组
     */
    @Query("""
            SELECT t FROM QtTaskGroupPo t WHERE t.name = :name
            """)
    QtTaskGroupPo getByName(@Param("name") String name);


}
