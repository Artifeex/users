package ru.sandr.users.core.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@Component
public class JwtBearerTokenInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            // 2. Достаем заголовок Authorization из входящего запроса
            String authHeader = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);

            // 3. Если токен есть, прокидываем его в исходящий запрос
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
            }
        }

        // 4. Продолжаем выполнение запроса
        return execution.execute(request, body);
    }
}
