package ru.sandr.users.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to finish password reset by one-time token")
public record ResetPasswordRequestDto(
        @Schema(description = "One-time reset token", example = "a3f4d2f1-86e9-4db9-8f18-9a7d13f5d001")
        @NotBlank(message = "token must not be blank")
        String token,
        @Schema(description = "New account password", example = "N3wStr0ngPass!")
        @NotBlank(message = "newPassword must not be blank")
        @Size(min = 10, message = "newPassword must be at least 10 characters")
        String newPassword
) {
}
