package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qfmodelgroup.QfModelGroupPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 流程模型分组Repository
 * 
 * @author KspTool(ksptool@outlook.com)
 * @since 2026-04-16
 * @license Proprietary
 * 版权所有 (c) 2026 KspTool及其贡献者保留所有权利。
 * 未经事先书面许可，严禁任何形式的复制或分发。
 */
@Repository
public interface QfModelGroupRepository extends JpaRepository<QfModelGroupPo, Long> {

    @Query("""
            SELECT u FROM QfModelGroupPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.code} IS NULL OR u.code LIKE CONCAT('%', :#{#po.code}, '%'))
            ORDER BY u.seq ASC, u.createTime DESC
            """ )
    Page<QfModelGroupPo> getQfModelGroupList(@Param("po") QfModelGroupPo po, Pageable pageable);

    /**
     * 根据编码统计流程模型分组数量 排除指定ID
     *
     * @param code 流程模型分组编码
     * @param id   流程模型分组ID
     * @return 流程模型分组数量
     */
    @Query("""
            SELECT COUNT(t) FROM QfModelGroupPo t
            WHERE t.code = :code AND (:id IS NULL OR t.id != :id)
            """)
    Long countByCodeExcludeId(@Param("code") String code, @Param("id") Long id);


}
