package com.ksptool.bio.biz.core.repository;

import com.ksptool.bio.biz.core.model.pack.MenuPackPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuPackRepository extends JpaRepository<MenuPackPo, Long> {
}
