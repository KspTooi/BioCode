package com.ksptool.bio.biz.core.common.config;

import com.ksptool.bio.biz.auth.service.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * @author KspTooi
 * @since 1.6.15(O).65
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        // 这里对接你的 SessionService
        return () -> {
            try {

                return Optional.ofNullable(SessionService.session().getUserId());

            } catch (Exception e) {
                return Optional.of(-10L);
            }
        };
    }
}