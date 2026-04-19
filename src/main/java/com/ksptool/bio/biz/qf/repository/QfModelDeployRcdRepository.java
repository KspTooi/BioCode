package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qfmodeldeployrcd.QfModelDeployRcdPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 流程模型部署记录Repository
 *
 * @author KspTool(ksptool@outlook.com)
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 * @since 2026-04-16
 */
@Repository
public interface QfModelDeployRcdRepository extends JpaRepository<QfModelDeployRcdPo, Long> {

    @Query("""
            SELECT u FROM QfModelDeployRcdPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status} )
            ORDER BY u.createTime DESC
            """)
    Page<QfModelDeployRcdPo> getQfModelDeployRcdList(@Param("po") QfModelDeployRcdPo po, Pageable pageable);

    /**
     * 查询最新可用的部署记录
     *
     * @param code     模型编码
     * @param pageable 分页参数
     * @return 部署记录列表
     */
    @Query("""
            SELECT u FROM QfModelDeployRcdPo u
            WHERE u.code = :code
            AND u.status = 0
            AND u.engProcessDefId IS NOT NULL
            ORDER BY u.version DESC
            LIMIT 1
            """)
    QfModelDeployRcdPo getLatestActiveByCode(@Param("code") String code);
}
