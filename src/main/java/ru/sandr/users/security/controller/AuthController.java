package ru.sandr.users.security.controller;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.sandr.users.core.dto.ApiErrorResponse;
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
@Tag(name = "Auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ResetPasswordTokenService resetPasswordTokenService;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Validates credentials and returns access token. Refresh token is set in HttpOnly cookie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
    @Operation(
            summary = "Refresh access token",
            description = "Issues new access and refresh tokens using refresh token from cookie."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed"),
            @ApiResponse(responseCode = "401", description = "Refresh token is absent or invalid",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        var authResult = authenticationService.refreshToken(refreshToken);
        ResponseCookie responseCookie = createRefreshTokenCookie(
                authResult.refreshToken(),
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
    @Operation(
            summary = "Request password reset",
            description = "Creates one-time password reset token and sends reset instructions."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Reset request accepted"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto) {
        resetPasswordTokenService.forgotPassword(forgotPasswordRequestDto);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Reset password by token",
            description = "Completes password reset flow using one-time token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Password successfully reset"),
            @ApiResponse(responseCode = "400", description = "Validation error or expired token",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public void resetPassword(@Valid @RequestBody ResetPasswordRequestDto requestDto) {
        resetPasswordTokenService.resetPassword(requestDto);
    }

}
