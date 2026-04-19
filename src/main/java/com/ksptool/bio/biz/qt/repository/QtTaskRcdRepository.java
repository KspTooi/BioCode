package com.ksptool.bio.biz.qt.repository;

import com.ksptool.bio.biz.qt.model.qttaskrcd.QtTaskRcdPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * QF任务调度日志Repository
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-02-10
 * @license Apache License 2.0
 */
@Repository
public interface QtTaskRcdRepository extends JpaRepository<QtTaskRcdPo, Long> {

    @Query("""
            SELECT u FROM QtTaskRcdPo u
            WHERE
            (:#{#po.id} IS NULL OR u.id  = :#{#po.id} )
            AND (:#{#po.taskId} IS NULL OR u.taskId  = :#{#po.taskId} )
            AND (:#{#po.taskName} IS NULL OR u.taskName  LIKE CONCAT('%', :#{#po.taskName}, '%') )
            AND (:#{#po.groupName} IS NULL OR u.groupName  LIKE CONCAT('%', :#{#po.groupName}, '%') )
            AND (:#{#po.target} IS NULL OR u.target  LIKE CONCAT('%', :#{#po.target}, '%') )
            AND (:#{#po.targetParam} IS NULL OR u.targetParam  LIKE CONCAT('%', :#{#po.targetParam}, '%') )
            AND (:#{#po.targetResult} IS NULL OR u.targetResult  LIKE CONCAT('%', :#{#po.targetResult}, '%') )
            AND (:#{#po.status} IS NULL OR u.status  = :#{#po.status} )
            AND (:#{#po.startTime} IS NULL OR u.startTime  = :#{#po.startTime} )
            AND (:#{#po.endTime} IS NULL OR u.endTime  = :#{#po.endTime} )
            AND (:#{#po.costTime} IS NULL OR u.costTime  = :#{#po.costTime} )
            AND (:#{#po.createTime} IS NULL OR u.createTime  = :#{#po.createTime} )
            ORDER BY u.startTime DESC
            """)
    Page<QtTaskRcdPo> getQtTaskRcdList(@Param("po") QtTaskRcdPo po, Pageable pageable);
}
