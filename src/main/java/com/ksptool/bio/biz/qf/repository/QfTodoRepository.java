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
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-16
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

    /**
     * 检查还有多少未完成的待办在用这个表单
     *
     * @param bizFormId 业务表单ID
     * @return 还有多少未完成的待办
     */
    @Query("""
            SELECT COUNT(1) FROM QfTodoPo u
            WHERE u.bizFormId = :bizFormId
            AND u.status = 0
            """)
    long countActiveTodyByBizFormId(@Param("bizFormId") Long bizFormId);
}
