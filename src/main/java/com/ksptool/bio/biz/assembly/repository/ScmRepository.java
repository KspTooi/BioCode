package com.ksptool.bio.biz.assembly.repository;

import com.ksptool.bio.biz.assembly.model.scm.ScmPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author KspTooi
 * @since 1.6.19(S).32
 */
@Repository
public interface ScmRepository extends JpaRepository<ScmPo, Long> {

    @Query("""
            SELECT u FROM ScmPo u
            WHERE
            (:#{#po.id} IS NULL OR u.id  = :#{#po.id} )
            AND (:#{#po.name} IS NULL OR u.name  LIKE CONCAT('%', :#{#po.name}, '%') )
            AND (:#{#po.projectName} IS NULL OR u.projectName  LIKE CONCAT('%', :#{#po.projectName}, '%') )
            ORDER BY u.createTime DESC
            """)
    Page<ScmPo> getScmList(@Param("po") ScmPo po, Pageable pageable);

    /**
     * 根据名称统计SCM数量
     *
     * @param name 名称
     * @return SCM数量
     */
    @Query("""
            SELECT COUNT(u) FROM ScmPo u WHERE u.name = :name
            """)
    int countByName(@Param("name") String name);

    /**
     * 根据名称统计SCM数量 排除指定ID
     *
     * @param name 名称
     * @param id   SCM ID
     * @return SCM数量
     */
    @Query("""
            SELECT COUNT(u) FROM ScmPo u WHERE u.name = :name AND u.id != :id
            """)
    int countByNameExcludeId(@Param("name") String name, @Param("id") Long id);
}
