package ru.sandr.users.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sandr.users.security.dto.AuthenticationRequestDto;
import ru.sandr.users.security.dto.AuthenticationResponseDto;
import ru.sandr.users.security.dto.ForgotPasswordRequestDto;
import ru.sandr.users.security.dto.ResetPasswordRequestDto;
import ru.sandr.users.security.service.AuthenticationService;
import ru.sandr.users.security.service.ResetPasswordTokenService;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ResetPasswordTokenService resetPasswordTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@Valid @RequestBody AuthenticationRequestDto authenticationRequestDto) {
        var loginResult = authenticationService.login(authenticationRequestDto);
        ResponseCookie responseCookie = createRefreshTokenCookie(
                loginResult.refreshToken(),
                loginResult.refreshTokenDuration()
        );
        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(new AuthenticationResponseDto(loginResult.accessToken()));
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        var authResult = authenticationService.refreshToken(refreshToken);
        ResponseCookie responseCookie = createRefreshTokenCookie(
                authResult.accessToken(),
                authResult.refreshTokenDuration()
        );
        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                             .body(new AuthenticationResponseDto(authResult.accessToken()));
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken, Duration refreshTokenDuration) {
        return ResponseCookie.from("refreshToken", refreshToken)
                             .httpOnly(true)  // JS не имеет доступа, поэтому защищены от XSS атаки, где js скрипт сможет вытащить refresh token из local storage
                             .secure(false)   // true для HTTPS
                             .path("/auth/")       // Cookie будет передаваться только по пути /auth/*
                             .maxAge(refreshTokenDuration.getSeconds())
                             .sameSite("Strict") // Отправлять только на тот же хост, с которого пришла
                             .build();
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequestDto) {
        resetPasswordTokenService.forgotPassword(forgotPasswordRequestDto);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resetPassword(ResetPasswordRequestDto requestDto) {
        resetPasswordTokenService.resetPassword(requestDto);
    }

}
