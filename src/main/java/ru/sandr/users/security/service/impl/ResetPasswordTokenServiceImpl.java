package ru.sandr.users.security.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.security.dto.ForgotPasswordRequestDto;
import ru.sandr.users.security.dto.ResetPasswordRequestDto;
import ru.sandr.users.security.entity.ResetPasswordToken;
import ru.sandr.users.security.repository.ResetPasswordTokenRepository;
import ru.sandr.users.security.service.ResetPasswordTokenService;
import ru.sandr.users.security.utils.HashUtils;
import ru.sandr.users.security.utils.PasswordAndTokenGenerator;
import ru.sandr.users.user.entity.User;
import ru.sandr.users.user.events.PasswordChangedEvent;
import ru.sandr.users.user.events.ResetPasswordEvent;
import ru.sandr.users.user.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResetPasswordTokenServiceImpl implements ResetPasswordTokenService {

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${tokens.resetPassword.length:15}")
    private Integer tokenLength;

    @Value("${tokens.resetPassword.expiration:15m}")
    private Duration duration;

    @Transactional
    @Override
    public void generateAndSaveResetPasswordTokenForUser(User user) {
        // Пока сохраняем токены в БД, но в идеале бы прикрутить redis
        var now = Instant.now();

        String rawToken = PasswordAndTokenGenerator.generate(tokenLength);
        var resetPasswordToken = ResetPasswordToken.builder()
                                                   .tokenHash(HashUtils.hashToken(rawToken))
                                                   .user(user)
                                                   .createdAt(now)
                                                   .expiryAt(now.plus(duration))
                                                   .build();
        resetPasswordTokenRepository.save(resetPasswordToken);

        String linkToResetEmail = null; // написать метод для генерации такой ссылки

        applicationEventPublisher.publishEvent(ResetPasswordEvent.builder()
                                                                 .userId(user.getId())
                                                                 .email(user.getEmail())
                                                                 .linkForResetEvent(linkToResetEmail)
                                                                 .build()
        );

        // Сохранить в outbox таблицу event и потом handler отправит в kafka message об этом
        // В event нужно передать:
        // email
        // Сообщение + ссылка на reset: http://localhost:8080/password/reset-password?token=rawToken - тут вернется frontend формф
        // Из которой сделатся запрос c передачей токена и новым паролем
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequestDto requestDto) {
        String tokenHash = HashUtils.hashToken(requestDto.token());
        // Как отреагировать на некорректный токен? Сообщить пользователю или нет?
        var resetToken = resetPasswordTokenRepository.findByTokenHash(tokenHash).orElseThrow();
        var user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(requestDto.newPassword()));
        applicationEventPublisher.publishEvent(PasswordChangedEvent.builder()
                                                                   .userId(user.getId())
                                                                   .email(user.getEmail())
                                                                   .build());
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        var userOptional = userRepository.findByEmail(forgotPasswordRequestDto.email().toLowerCase());
        // Сгенерить токен, сохранить его в БД(в идеале бы в redis с указанием ttl, но пока в БД)
        userOptional.ifPresent(this::generateAndSaveResetPasswordTokenForUser);
    }
}
