package com.ksptool.bio.biz.core.repository;


import com.ksptool.bio.biz.core.model.attach.AttachPo;
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

}
