package com.ksptool.bio.biz.qf.repository;

import com.ksptool.bio.biz.qf.model.qftodo.QfTodoPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QfTodoRepository extends JpaRepository<QfTodoPo, Long> {

    @Query("""
            SELECT u FROM QfTodoPo u
            WHERE
            (:#{#po.summary} IS NULL OR u.summary LIKE CONCAT('%', :#{#po.summary}, '%'))
            AND (:#{#po.memberType} IS NULL OR u.memberType = :#{#po.memberType} )
            AND (:#{#po.memberId} IS NULL OR u.memberId = :#{#po.memberId} )
            AND (:#{#po.initiatorId} IS NULL OR u.initiatorId = :#{#po.initiatorId} )
            AND (:#{#po.createTime} IS NULL OR u.createTime = :#{#po.createTime} )
            ORDER BY u.createTime DESC
            """)
    Page<QfTodoPo> getQfTodoList(@Param("po") QfTodoPo po, Pageable pageable);
}
