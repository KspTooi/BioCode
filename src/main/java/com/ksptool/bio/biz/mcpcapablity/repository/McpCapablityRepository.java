package com.ksptool.bio.biz.mcpcapablity.repository;

import com.ksptool.bio.biz.mcpcapablity.model.McpCapablityPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface McpCapablityRepository extends JpaRepository<McpCapablityPo, Long>{

    @Query("""
    SELECT u FROM McpCapablityPo u
    WHERE
    ORDER BY u.createTime DESC
    """)
    Page<McpCapablityPo> getMcpCapablityList(@Param("po") McpCapablityPo po, Pageable pageable);
}
