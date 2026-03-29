package ru.sandr.users.security.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import ru.sandr.users.core.exception.BadRequestException;
import ru.sandr.users.core.exception.ObjectNotFoundException;
import ru.sandr.users.security.dto.AuthenticationRequestDto;
import ru.sandr.users.security.dto.AuthResultDto;
import ru.sandr.users.security.dto.ForgotPasswordRequestDto;
import ru.sandr.users.security.dto.ResetPasswordRequestDto;
import ru.sandr.users.security.entity.RefreshToken;
import ru.sandr.users.security.repository.RefreshTokenRepository;
import ru.sandr.users.security.utils.CustomUserDetails;
import ru.sandr.users.security.utils.HashUtils;
import ru.sandr.users.security.utils.JwtUtils;
import ru.sandr.users.user.dto.ChangePasswordRequest;
import ru.sandr.users.user.entity.User;
import ru.sandr.users.user.events.PasswordChangedEvent;
import ru.sandr.users.user.repository.UserRepository;
import ru.sandr.users.core.exception.UnauthorizedException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${tokens.refresh.expiration:30d}")
    private Duration refreshExpiration;

    @Value("${tokens.refresh.tokensForOneUser:5}")
    private Integer countRefreshTokensForOneUser;

    @Transactional
    public AuthResultDto login(@RequestBody AuthenticationRequestDto authenticationRequestDto) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authenticationRequestDto.login(),
                authenticationRequestDto.password()
        );
        // authenticate метод не кладет authentication объект в SecurityContextHolder!
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        var userDetails = (CustomUserDetails) authentication.getPrincipal(); // Именно в Principal хранится наш UserDetails класс
        // Сгенерировать accessToken
        var accessToken = jwtUtils.generateAccessToken(userDetails);
        // Сгенерировать новый refreshToken и не забыть про ограничение в 5 токенов
        var refreshToken = generateRefreshToken(userDetails);
        return new AuthResultDto(accessToken, refreshToken, refreshExpiration);
    }

    @Transactional
    public String generateRefreshToken(CustomUserDetails userDetails) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = HashUtils.hashToken(rawToken);
        var user = userDetails.getUser();
        var refreshTokenEntity = RefreshToken.builder()
                                             .tokenHash(tokenHash)
                                             .user(user)
                                             .createdAt(Instant.now())
                                             .expiryAt(Instant.now().plus(refreshExpiration))
                                             .build();

        refreshTokenRepository.save(refreshTokenEntity);
        refreshTokenRepository.deleteTokensOlderThanTopN(
                user.getId(),
                countRefreshTokensForOneUser
        );

        return rawToken;
    }

    @Transactional
    public AuthResultDto refreshToken(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new UnauthorizedException("REFRESH_TOKEN_MISSING", "Missing refresh token");
        }

        String tokenHash = HashUtils.hashToken(refreshToken);
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                                                                .orElseThrow(() -> new UnauthorizedException(
                                                                        "REFRESH_TOKEN_INVALID",
                                                                        "Invalid refresh token"
                                                                ));

        Instant now = Instant.now();
        if (!refreshTokenEntity.getExpiryAt().isAfter(now)) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new UnauthorizedException("REFRESH_TOKEN_EXPIRED", "Refresh token expired");
        }

        var userDetails = new CustomUserDetails(refreshTokenEntity.getUser());
        var accessToken = jwtUtils.generateAccessToken(userDetails);

        String newRawRefreshToken = UUID.randomUUID().toString();
        String newRefreshTokenHash = HashUtils.hashToken(newRawRefreshToken);

        refreshTokenEntity.setTokenHash(newRefreshTokenHash);
        refreshTokenEntity.setCreatedAt(now);
        refreshTokenEntity.setExpiryAt(now.plus(refreshExpiration));
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResultDto(accessToken, newRawRefreshToken, refreshExpiration);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        var user = getCurrentUser();
        if (!isPasswordsMatch(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("WRONG_PASSWORD", "Wrong password");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        eventPublisher.publishEvent(PasswordChangedEvent.builder()
                                                        .email(user.getEmail())
                                                        .userId(user.getId())
                                                        .build()
        );
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object username = auth.getPrincipal();
        return userRepository.findByUsernameOrEmail(username.toString(), username.toString())
                             .orElseThrow(() -> new ObjectNotFoundException(
                                     "USER_NOT_FOUND",
                                     "User not found: " + username
                             ));
    }

    public boolean isPasswordsMatch(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}
