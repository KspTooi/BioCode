package com.ksptool.bio.biz.core.repository;


import com.ksptool.bio.biz.core.model.attach.AttachPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachRepository extends JpaRepository<AttachPo, Long> {


    /**
     * 根据SHA256和业务代码获取已经上传完成的附件
     *
     * @param sha256 SHA256
     * @param kind   业务代码
     * @return 附件
     */
    @Query("""
            SELECT t FROM AttachPo t
            WHERE
            t.sha256 = :sha256 AND
            t.kind = :kind
            """)
    AttachPo getBySha256AndKind(@Param("sha256") String sha256, @Param("kind") String kind);


    /**
     * 查询需要校验的文件附件
     *
     * @param limit 查询条数
     * @return 文件附件列表
     */
    @Query("""
            SELECT t FROM AttachPo t
            WHERE t.status = 2
            ORDER BY t.createTime
            LIMIT :limit
            """)
    List<AttachPo> getNeedVerifyAttachList(@Param("limit") int limit);

    /**
     * 统计有效附件数量
     *
     * @return 有效附件总数
     */
    @Query("""
            SELECT COUNT(t) FROM AttachPo t
            WHERE t.status = 3
            """)
    long countValidAttaches();

    /**
     * 分页查询附件列表
     *
     * @param po       查询条件
     * @param pageable 分页条件
     * @return 附件列表
     */
    @Query("""
            SELECT t FROM AttachPo t
            WHERE (:#{#po.kind} IS NULL OR t.kind = :#{#po.kind})
            AND (
                :indexFilter IS NULL
                OR (:indexFilter = 1 AND t.status = 3)
                OR (:indexFilter = 0 AND t.status <> 3)
            )
            ORDER BY t.createTime DESC
            """)
    Page<AttachPo> getAttachList(@Param("po") AttachPo po, @Param("indexFilter") Integer indexFilter, Pageable pageable);

    /**
     * 分页查询有效附件列表
     *
     * @param pageable 分页条件
     * @return 有效附件列表
     */
    @Query("""
            SELECT t FROM AttachPo t
            WHERE t.status = 3
            ORDER BY t.id ASC
            """)
    Page<AttachPo> getValidAttachList(Pageable pageable);

    /**
     * 分页查询无效附件列表
     *
     * @param pageable 分页条件
     * @return 无效附件列表
     */
    @Query("""
            SELECT t FROM AttachPo t
            WHERE t.status <> 3
            ORDER BY t.id ASC
            """)
    Page<AttachPo> getInvalidAttachList(Pageable pageable);

    /**
     * 按摘要查询附件列表
     *
     * @param sha256 文件摘要
     * @return 附件列表
     */
    @Query("""
            SELECT t FROM AttachPo t
            WHERE t.sha256 = :sha256
            """)
    List<AttachPo> getBySha256(@Param("sha256") String sha256);

    /**
     * 查询全部索引路径
     *
     * @return 路径列表
     */
    @Query("""
            SELECT t.path FROM AttachPo t
            """)
    List<String> getAllPaths();

}
