package ru.sandr.users.core.config;

import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.CompositeProducerInterceptor;

import java.util.Map;

@Configuration
@Getter
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> outboxProducerConfig(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties();

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // String, потому что в БД уже хранится JSON строка, которую в том же виде и отправим в топик
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> outboxKafkaTemplate(@Qualifier("outboxProducerConfig") ProducerFactory<String, String> outboxProducerConfig) {
        return new KafkaTemplate<>(outboxProducerConfig);
    }
}
