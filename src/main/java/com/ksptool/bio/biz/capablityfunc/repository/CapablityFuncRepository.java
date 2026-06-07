package com.ksptool.bio.biz.capablityfunc.repository;

import com.ksptool.bio.biz.capablityfunc.model.CapablityFuncPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CapablityFuncRepository extends JpaRepository<CapablityFuncPo, Long>{

    @Query("""
    SELECT u FROM CapablityFuncPo u
    WHERE
    ORDER BY u.createTime DESC
    """)
    Page<CapablityFuncPo> getCapablityFuncList(@Param("po") CapablityFuncPo po, Pageable pageable);
}
