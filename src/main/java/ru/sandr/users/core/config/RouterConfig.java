package ru.sandr.users.core.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "router")
@Getter
@Setter
@NoArgsConstructor
public class RouterConfig {
    private String fileTopicName;
    private String userTopicName;
}
