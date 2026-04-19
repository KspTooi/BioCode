package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qfbizform.QfBizFormPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 业务表单Repository
 * 
 * @author WangQingHua(603484930@qq.com)
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Repository
public interface QfBizFormRepository extends JpaRepository<QfBizFormPo, Long> {

    @Query("""
            SELECT u FROM QfBizFormPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.tableName} IS NULL OR u.tableName LIKE CONCAT('%', :#{#po.tableName}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            AND (:#{#po.seq} IS NULL OR u.seq = :#{#po.seq} )
            ORDER BY u.createTime DESC
            """)
    Page<QfBizFormPo> getBizFormList(@Param("po") QfBizFormPo po, Pageable pageable);

    /**
     * 查询最新可用的业务表单
     *
     * @param code 业务表单编码
     * @return 业务表单
     */
    @Query("""
            SELECT u FROM QfBizFormPo u
            WHERE u.code = :code
            AND u.status = 0
            ORDER BY u.createTime DESC
            LIMIT 1
            """)
    QfBizFormPo getActiveByCode(@Param("code") String code);

}
