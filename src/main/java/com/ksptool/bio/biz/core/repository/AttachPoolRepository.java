package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.attachpool.AttachPoolPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AttachPoolRepository extends JpaRepository<AttachPoolPo, Long> {


    /**
     * 查询最新的附件池扫描记录
     * @return 最新的附件池扫描记录
     */
    @Query("""
            SELECT t FROM AttachPoolPo t
            ORDER BY t.createTime DESC, t.id DESC
            LIMIT 1
            """)
    AttachPoolPo getLatestScanRecord();

}
