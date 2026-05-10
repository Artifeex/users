package ru.sandr.users.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.sandr.users.core.service.JwtBearerTokenInterceptor;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient fileServiceClient(
            RestClient.Builder builder,
            @Value("${api.file-service-base-url}") String fileServiceBaseUrl,
            JwtBearerTokenInterceptor jwtBearerTokenInterceptor
    ) {
        return builder.baseUrl(fileServiceBaseUrl)
                      .requestInterceptor(jwtBearerTokenInterceptor)
                      .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                      .requestFactory(createRequestFactory(Duration.ofSeconds(5L), Duration.ofSeconds(5L)))
                      .build();
    }

    // Вспомогательный метод для настройки таймаутов
    private JdkClientHttpRequestFactory createRequestFactory(Duration readTimeout, Duration connectTimeout) {
        // Настраиваем встроенный в Java HttpClient (Connection Pooling работает из коробки)
        HttpClient jdkHttpClient = HttpClient.newBuilder()
                                             .connectTimeout(connectTimeout)
                                             .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdkHttpClient);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
