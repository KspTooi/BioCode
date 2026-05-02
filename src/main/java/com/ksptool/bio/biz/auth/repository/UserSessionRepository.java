package com.ksptool.bio.biz.auth.repository;

import com.ksptool.bio.biz.auth.model.session.UserSessionPo;
import com.ksptool.bio.biz.auth.model.session.dto.GetSessionListDto;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionPo, Long> {

    /**
     * 获取会话列表
     *
     * @param dto  查询条件
     * @param page 分页信息
     * @return 会话列表
     */
    @Query("""
            SELECT
            us.id AS id,
            u.username AS username,
            us.rsMax AS rsMax,
            us.createTime AS createTime,
            us.expiresAt AS expiresAt
            FROM UserSessionPo us
            LEFT JOIN UserPo u ON us.userId = u.id
            WHERE (:#{#dto.userName} IS NULL OR u.username LIKE %:#{#dto.userName}%)
            ORDER BY us.createTime DESC
            """)
    Page<Tuple> getSessionList(@Param("dto") GetSessionListDto dto, Pageable page);


    /**
     * 根据SessionId获取会话
     *
     * @param sessionId 会话SessionId
     * @return 会话
     */
    @Query("""
                SELECT us FROM UserSessionPo us
                WHERE us.sessionId = :sessionId
            """)
    UserSessionPo getSessionBySessionId(@Param("sessionId") String sessionId);


    /**
     * 根据用户ID获取会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @Query("""
                SELECT us FROM UserSessionPo us
                WHERE us.userId = :userId
            """)
    List<UserSessionPo> getSessionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户IDS获取未过期的会话
     * 在线用户的判断标准是会话未过期（expiresAt > 当前时间）
     *
     * @param userIds 用户IDS
     * @return 会话列表
     */
    @Query("""
                SELECT us FROM UserSessionPo us
                WHERE us.userId IN :userIds AND us.expiresAt > CURRENT_TIMESTAMP
            """)
    List<UserSessionPo> getSessionByUserIds(@Param("userIds") List<Long> userIds);


    /**
     * 根据用户ID列表删除会话
     *
     * @param userIds 用户ID列表
     * @return 删除的会话数量
     */
    @Modifying
    @Query("""
            DELETE FROM UserSessionPo us
            WHERE us.userId IN :userIds
            """)
    int removeUserSessionByUserIds(@Param("userIds") List<Long> userIds);


    /**
     * 根据用户ID列表获取未过期的用户ID列表
     * 在线用户的判断标准是会话未过期（expiresAt > 当前时间）
     *
     * @param userIds 用户ID列表
     * @return 用户ID列表
     */
    @Query("""
            SELECT us.userId FROM UserSessionPo us
            WHERE us.userId IN :userIds AND us.expiresAt > NOW()    
            """)
    List<Long> getOnlineUserIdsByUserIds(@Param("userIds") List<Long> userIds);


    /**
     * 根据租户ID获取未过期的用户ID列表
     * 在线用户的判断标准是会话未过期（expiresAt > 当前时间）
     *
     * @param rootId 租户ID
     * @return 用户ID列表
     */
    @Query("""
            SELECT us.userId FROM UserSessionPo us
            WHERE us.rootId = :rootId AND us.expiresAt > NOW()
            """)
    List<Long> getOnlineUserIdsByRootId(@Param("rootId") Long rootId);

}