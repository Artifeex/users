package ru.sandr.users.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.sandr.users.security.filter.JwtAuthenticationFilter;
import ru.sandr.users.security.handler.FilterChainExceptionHandler;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            FilterChainExceptionHandler filterChainExceptionHandler,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable) // Для jwt нет смысла, т.к. мы не основываемся на JSESSION ID
                   .cors(cors -> cors.configurationSource(corsConfigurationSource))
                   .httpBasic(AbstractHttpConfigurer::disable)
                   .formLogin(AbstractHttpConfigurer::disable)
                   .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
                           SessionCreationPolicy.STATELESS)
                   )
                   .addFilterBefore(jwtAuthenticationFilter, BasicAuthenticationFilter.class)
                   .authorizeHttpRequests(
                           auth ->
                                   auth.requestMatchers(
                                               "/swagger-ui.html",
                                               "/swagger-ui/**",
                                               "/v3/api-docs",
                                               "/v3/api-docs/**",
                                               "/actuator/health",
                                               "/actuator/prometheus"
                                       )
                                       .permitAll()
                                       .requestMatchers("/auth/**").permitAll()
                                       .requestMatchers("/error").permitAll()
                                       .requestMatchers("/.well-known/jwks.json").permitAll()
                                       .requestMatchers("/admin/**").hasRole("ADMIN")
                                       .requestMatchers("/hierarchy/**").hasRole("ADMIN")
                                       .requestMatchers("/teachers/**").hasRole("TEACHER")
                                       .requestMatchers("/api/v1/import/**").hasRole("ADMIN")
                                       .anyRequest().authenticated()
                   ).exceptionHandling(customizer ->
                        customizer.authenticationEntryPoint(filterChainExceptionHandler)
                                  .accessDeniedHandler(filterChainExceptionHandler))
                   .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
