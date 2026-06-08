package ru.sandr.users.core.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryTracingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import javax.sql.DataSource;

/**
 * Hibernate/JPA не создаёт Micrometer spans на SQL — только метрики.
 * OpenTelemetry JDBC оборачивает пул и пишет span на каждый запрос к PostgreSQL.
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({JdbcTelemetry.class, OpenTelemetry.class})
@AutoConfigureAfter(OpenTelemetryTracingAutoConfiguration.class)
@AutoConfigureBefore({DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class DataSourceTracingConfiguration {

    @Bean
    static BeanPostProcessor otelJdbcDataSourceWrapper(ObjectProvider<OpenTelemetry> openTelemetry) {
        return new OtelJdbcDataSourceWrapper(openTelemetry);
    }

    private static final class OtelJdbcDataSourceWrapper implements BeanPostProcessor, PriorityOrdered {

        private final ObjectProvider<OpenTelemetry> openTelemetry;

        private OtelJdbcDataSourceWrapper(ObjectProvider<OpenTelemetry> openTelemetry) {
            this.openTelemetry = openTelemetry;
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (!(bean instanceof DataSource dataSource)) {
                return bean;
            }
            if (!beanName.equals("dataSource")) {
                return bean;
            }
            if (dataSource.getClass().getName().contains("OpenTelemetry")) {
                return bean;
            }
            OpenTelemetry otel = openTelemetry.getObject();
            DataSource wrapped = JdbcTelemetry.create(otel).wrap(dataSource);
            log.info("OpenTelemetry JDBC tracing enabled for bean '{}'", beanName);
            return wrapped;
        }
    }
}
