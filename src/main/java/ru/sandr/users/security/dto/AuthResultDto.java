package ru.sandr.users.security.dto;

import java.time.Duration;

/**
 * DTO для операций login и refreshToken. Возвращает новую пару access и refreshToken
 * @param accessToken
 * @param refreshToken
 * @param refreshTokenDuration
 */
public record AuthResultDto(String accessToken, String refreshToken, Duration refreshTokenDuration) {
}
