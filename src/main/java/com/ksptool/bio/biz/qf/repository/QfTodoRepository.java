package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 待办Repository
 * 
 * @author WangQingHua(603484930@qq.com)
 * @author (Ish)Yuumi(1144150092@qq.com)
 * @author Akkarin(1075613357@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Repository
public interface QfTodoRepository extends JpaRepository<QfTodoPo, Long> {

    @Query("""
            SELECT u FROM QfTodoPo u
            WHERE
            (:#{#po.summary} IS NULL OR u.summary LIKE CONCAT('%', :#{#po.summary}, '%'))
            AND (:#{#po.memberType} IS NULL OR u.memberType = :#{#po.memberType} )
            AND (:#{#po.memberId} IS NULL OR u.memberId = :#{#po.memberId} )
            AND (:#{#po.initiatorId} IS NULL OR u.initiatorId = :#{#po.initiatorId} )
            AND (:#{#po.createTime} IS NULL OR u.createTime = :#{#po.createTime} )
            ORDER BY u.createTime DESC
            """)
    Page<QfTodoPo> getQfTodoList(@Param("po") QfTodoPo po, Pageable pageable);
}
