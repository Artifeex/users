package ru.sandr.users.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response with issued access token")
public record AuthenticationResponseDto(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) { }
