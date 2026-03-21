package ru.sandr.users.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.sandr.users.security.entity.RefreshToken;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${tokens.jwt.secret}")
    private String secret;

    @Value("${tokens.jwt.access.expiration}")
    private Duration accessExpiration;

    @Value("${tokens.jwt.claim.roles:roles}")
    private String roleClaimName;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Создаем объект ключа(симметричный, т.е. им можно как проверять, так и генерировать токены)
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put(roleClaimName, roles);
        // Так же стоит передавать в токене инфу о том, где находится студент ?

        Instant now = Instant.now();
        Instant accessExpirationInstant = now.plus(accessExpiration);


        return Jwts.builder()
                   .claims(claims) // наши кастомные поля
                   .subject(userDetails.getUsername()) // стандартное поле sub
                   .issuedAt(Date.from(now)) // стандартное поле iat
                   .expiration(Date.from(accessExpirationInstant)) // стандартное поле exp
                   .signWith(key) // подписыаем
                   .compact();
    }

    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get(roleClaimName));
    }

    // Валидация токена
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Извлечение имени пользователя (Subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                   .verifyWith(key) // Проверяем подпись тем же ключом
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }
}
