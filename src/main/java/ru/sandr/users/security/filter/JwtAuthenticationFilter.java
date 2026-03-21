package ru.sandr.users.security.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.sandr.users.core.exception.UnauthorizedException;
import ru.sandr.users.security.utils.JwtUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final int JWT_BEGIN_INDEX_IN_BEARER = 7;
    private final JwtUtils jwtUtils;
    // С помощью этого resolvera можно попросить Spring вызвать наш ControllerAdvice с нужным методом
    // Только исключения, выброшенные в dispatcherServlet(который уже вызвыает методы контроллера и отрабатывает после фильтров)
    // Попадают в ControllerAdvice. Поэтому через HandlerExceptionResolver мы можем обойти эту проблему и не писать
    // Какую-то отдельную обработку таких ошибок
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER)) {
            if (!authorizeByJwtToken(request, response, authHeader)) {
                // Ответ уже записан через HandlerExceptionResolver → не продолжаем цепочку,
                // иначе Spring Security снова ответит 401 (дублирующий JSON в теле).
                return;
            }
        }
        // Даем возможность другим фильтрам выполнить аутентификацию, если вдруг произошла аутентификация не через jwt
        filterChain.doFilter(request, response);

    }

    /**
     * @return false если исключение обработано и цепочку фильтров продолжать нельзя
     */
    private boolean authorizeByJwtToken(HttpServletRequest request, HttpServletResponse response, String authHeader) {
        try {
            String username;
            String jwt;
            jwt = authHeader.substring(JWT_BEGIN_INDEX_IN_BEARER);
            username = jwtUtils.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var roles = jwtUtils.extractRoles(jwt).stream().map(SimpleGrantedAuthority::new).toList();
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        roles
                );
                // Кладем Authentication объект, чтобы другие фильтры аутентификации не выполняли проверки
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.info("JWT has expired");
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new UnauthorizedException("TOKEN_EXPIRED", "Срок действия токена истек")
            );
            return false;
        } catch (SignatureException e) {
            log.info("JWT signature exception");
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new UnauthorizedException("INVALID_SIGNATURE", "Неверная подпись токена") // Возможно друг
            );
            return false;
        } catch (MalformedJwtException e) {
            log.info("JWT malformed JWT");
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new UnauthorizedException("INVALID_JWT_FORMAT", "Некорректный формат токена")
            );
            return false;
        }
    }

}
