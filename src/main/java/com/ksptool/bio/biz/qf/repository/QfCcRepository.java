package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qfcc.QfCcPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 抄送Repository
 *
 * @author Akkarin(1075613357@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-17
 */
@Repository
public interface QfCcRepository extends JpaRepository<QfCcPo, Long> {

    @Query("""
            SELECT u FROM QfCcPo u
            WHERE
            (:#{#po.summary} IS NULL OR u.summary LIKE CONCAT('%', :#{#po.summary}, '%'))
            AND (:#{#po.fromName} IS NULL OR u.fromName LIKE CONCAT('%', :#{#po.fromName}, '%'))
            AND (:#{#po.isRead} IS NULL OR u.isRead = :#{#po.isRead} )
            ORDER BY u.createTime DESC
            """)
    Page<QfCcPo> getQfCcList(@Param("po") QfCcPo po, Pageable pageable);
}
