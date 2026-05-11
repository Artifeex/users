package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to change authenticated user password")
public record ChangePasswordRequest(
        @Schema(description = "Current password for verification", example = "OldP@ssw0rd!")
        @NotBlank(message = "oldPassword must not be blank") String oldPassword,

        @Schema(description = "New password", example = "N3wP@ssw0rd!")
        @Size(min = 8, message = "newPassword must be at least 8 characters")
        @NotBlank(message = "newPassword must not be blank")
        String newPassword 
) {
}
