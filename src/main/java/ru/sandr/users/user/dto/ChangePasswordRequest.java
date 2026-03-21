package ru.sandr.users.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,

        @Size(min = 8, message = "Длина пароля не может быть менее 8 символов")
        @NotBlank String newPassword 
) {
}
