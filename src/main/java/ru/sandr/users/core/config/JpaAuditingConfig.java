package ru.sandr.users.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    private static final String SYSTEM_AUDITOR = "system";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of(SYSTEM_AUDITOR);
            }
            String name = auth.getName();
            if ("anonymousUser".equals(name)) {
                return Optional.of(SYSTEM_AUDITOR);
            }
            return Optional.of(name);
        };
    }
}
