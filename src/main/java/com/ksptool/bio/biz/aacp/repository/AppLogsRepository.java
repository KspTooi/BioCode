package com.ksptool.bio.biz.aacp.repository;

import com.ksptool.bio.biz.aacp.model.applogs.AacpAppLogsPo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppLogsRepository extends JpaRepository<AacpAppLogsPo, Long> {

    @Query("""
            SELECT u FROM AacpAppLogsPo u
            WHERE
            (:#{#po.appId} IS NULL OR u.appId = :#{#po.appId} )
            AND (:#{#po.providerId} IS NULL OR u.providerId = :#{#po.providerId} )
            AND (:#{#po.modelId} IS NULL OR u.modelId = :#{#po.modelId} )
            AND (:#{#po.inputToken} IS NULL OR u.inputToken = :#{#po.inputToken} )
            AND (:#{#po.outputToken} IS NULL OR u.outputToken = :#{#po.outputToken} )
            AND (:#{#po.cost} IS NULL OR u.cost LIKE CONCAT('%', :#{#po.cost}, '%'))
            AND (:#{#po.startTime} IS NULL OR u.startTime = :#{#po.startTime} )
            AND (:#{#po.endTime} IS NULL OR u.endTime = :#{#po.endTime} )
            AND (:#{#po.durationMs} IS NULL OR u.durationMs = :#{#po.durationMs} )
            AND (:#{#po.ttfbMs} IS NULL OR u.ttfbMs = :#{#po.ttfbMs} )
            AND (:#{#po.statusCode} IS NULL OR u.statusCode LIKE CONCAT('%', :#{#po.statusCode}, '%'))
            AND (:#{#po.clientIp} IS NULL OR u.clientIp LIKE CONCAT('%', :#{#po.clientIp}, '%'))
            ORDER BY u.createTime DESC
            """)
    Page<AacpAppLogsPo> getAppLogsList(@Param("po") AacpAppLogsPo po, Pageable pageable);
}
