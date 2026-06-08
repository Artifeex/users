package ru.sandr.users.core.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Дополнительные правила трейсинга поверх {@code management.observations.*} в application.yml.
 */
@Configuration
public class TracingConfig {

    /**
     * Не создавать трейсы для actuator (health/prometheus) — они засоряют Tempo при частых probe.
     */
    @Bean
    ObservationPredicate skipActuatorObservations() {
        return (name, context) -> {
            if (!name.startsWith("http.server.requests")) {
                return true;
            }
            Object path = context.getLowCardinalityKeyValue("uri") != null
                    ? context.getLowCardinalityKeyValue("uri").getValue()
                    : null;
            if (path == null) {
                return true;
            }
            String uri = path.toString();
            return !uri.startsWith("/actuator");
        };
    }
}
