package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import com.ksptool.bio.biz.qf.model.qftodo.dto.GetQfTodoListDto;
import jakarta.persistence.Tuple;

import java.util.List;

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

    /**
     * 查询待办事项列表
     *
     * @param dto 查询条件
     * @param uid 用户ID
     * @param gIds 用户组ID列表
     * @param pageable 分页条件
     * @return 待办事项列表
     */
    @Query("""
            SELECT
            u.id AS id,
            u.nodeName AS nodeName,
            f.name AS bizFormName,
            u.initiatorName AS initiatorName,
            u.summary AS summary,
            u.status AS status,
            u.createTime AS createTime
            FROM QfTodoPo u
            LEFT JOIN QfBizFormPo f ON u.bizFormId = f.id
            WHERE
            (
                (u.memberType = 0 AND u.memberId = :uid)
                OR (u.memberType = 1 AND u.memberId IN :gIds)
            )
            AND (:#{#dto.nodeName} IS NULL OR u.summary LIKE CONCAT('%', :#{#dto.nodeName}, '%'))
            AND (:#{#dto.bizFormId} IS NULL OR u.bizFormId = :#{#dto.bizFormId})
            AND (:#{#dto.status} IS NULL OR u.status = :#{#dto.status})
            ORDER BY u.createTime DESC
            """)
    Page<Tuple> getQfTodoList(@Param("dto") GetQfTodoListDto dto,
                              @Param("uid") Long uid,
                              @Param("gIds") List<Long> gIds,
                              Pageable pageable);

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
