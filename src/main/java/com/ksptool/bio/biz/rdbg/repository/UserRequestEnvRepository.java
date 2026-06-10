package com.ksptool.bio.biz.rdbg.repository;

import com.ksptool.bio.biz.rdbg.model.userrequestenv.UserRequestEnvPo;
import com.ksptool.bio.biz.rdbg.model.userrequestenv.vo.GetUserRequestEnvListVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRequestEnvRepository extends JpaRepository<UserRequestEnvPo, Long> {

    @Query("""
            SELECT new com.ksptool.bio.biz.rdbg.model.userrequestenv.vo.GetUserRequestEnvListVo(
                u.id,
                u.user.id,
                u.name,
                u.remark,
                u.createTime,
                u.updateTime,
                CASE WHEN EXISTS(SELECT 1 FROM UserPo u2 WHERE u2.id = :userId AND u2.id = u.id) THEN 1 ELSE 0 END
            )
            FROM UserRequestEnvPo u
            WHERE
            u.user.id = :userId
            AND (:#{#po.name} IS NULL OR u.name LIKE CONCAT('%', :#{#po.name}, '%'))
            ORDER BY u.createTime DESC
            """)
    Page<GetUserRequestEnvListVo> getUserRequestEnvList(@Param("po") UserRequestEnvPo po, @Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT u FROM UserRequestEnvPo u
            WHERE
            u.id = :id AND u.user.id = :userId
            """)
    UserRequestEnvPo getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
