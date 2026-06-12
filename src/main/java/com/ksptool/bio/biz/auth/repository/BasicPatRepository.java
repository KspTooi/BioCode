package com.ksptool.bio.biz.auth.repository;

import com.ksptool.bio.biz.auth.model.basicpat.BasicPatPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author KspTool
 * @since 1.7.5(E).1
 */
@Repository
public interface BasicPatRepository extends JpaRepository<BasicPatPo, Long> {

    /**
     * 根据PAT哈希查询PAT
     *
     * @param patHash PAT哈希
     * @return PAT 不存在或已禁用则返回null
     */
    @Query("""
            SELECT u FROM BasicPatPo u
            WHERE u.patHash = :patHash AND u.status = 1
            """)
    BasicPatPo getPatByHash(@Param("patHash") String patHash);

    /**
     * 查询PAT列表
     *
     * @param po 查询条件
     * @param pageable 分页条件
     * @return PAT列表
     */
    @Query("""
            SELECT u FROM BasicPatPo u
            WHERE
            (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            AND (:#{#po.status} IS NULL OR u.status = :#{#po.status})
            AND (:#{#po.userId} IS NULL OR u.userId = :#{#po.userId})
            ORDER BY u.createTime DESC
            """)
    Page<BasicPatPo> getBasicPatList(@Param("po") BasicPatPo po, Pageable pageable);
}
