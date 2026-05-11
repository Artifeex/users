package ru.sandr.users.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for username/password authentication")
public record AuthenticationRequestDto(
        @Schema(description = "Login identifier (username or email depending on auth strategy)", example = "teacher_ivanov")
        @NotBlank(message = "login must not be blank")
        String login,
        @Schema(description = "Raw user password", example = "P@ssw0rd123")
        @NotBlank(message = "password must not be blank")
        String password
) { }
