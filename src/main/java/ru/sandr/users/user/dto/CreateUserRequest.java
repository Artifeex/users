package ru.sandr.users.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String username, // Уникальный идентификато - либо номер студенчекого либо номер у препода
        @NotBlank @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String middleName,
        @NotNull RoleName role,
        Long groupId,
        Long departmentId
) {}
