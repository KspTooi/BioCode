package com.ksptool.bio.biz.groupmenu.repository;

import com.ksptool.bio.biz.groupmenu.model.GroupMenuPo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface GroupMenuRepository extends JpaRepository<GroupMenuPo, Long>{

    @Query("""
    SELECT u FROM GroupMenuPo u
    WHERE
    ORDER BY u.createTime DESC
    """)
    Page<GroupMenuPo> getGroupMenuList(@Param("po") GroupMenuPo po, Pageable pageable);
}
