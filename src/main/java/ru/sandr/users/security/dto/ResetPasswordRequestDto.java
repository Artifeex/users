package ru.sandr.users.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank
        String token,
        @NotBlank
        @Size(min = 10, message = "Слишком короткий пароль")
        String newPassword
) {
}
