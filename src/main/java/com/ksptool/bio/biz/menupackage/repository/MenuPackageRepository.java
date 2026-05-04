package com.ksptool.bio.biz.menupackage.repository;

import com.ksptool.bio.biz.menupackage.model.MenuPackagePo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MenuPackageRepository extends JpaRepository<MenuPackagePo, Long>{

    @Query("""
    SELECT u FROM MenuPackagePo u
    WHERE
    ORDER BY u.createTime DESC
    """)
    Page<MenuPackagePo> getMenuPackageList(@Param("po") MenuPackagePo po, Pageable pageable);
}
