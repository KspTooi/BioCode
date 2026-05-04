package com.ksptool.bio.biz.rootpackage.repository;

import com.ksptool.bio.biz.rootpackage.model.RootPackagePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RootPackageRepository extends JpaRepository<RootPackagePo, Long>{

    @Query("""
    SELECT u FROM RootPackagePo u
    WHERE
    ORDER BY u.createTime DESC
    """)
    Page<RootPackagePo> getRootPackageList(@Param("po") RootPackagePo po, Pageable pageable);
}
