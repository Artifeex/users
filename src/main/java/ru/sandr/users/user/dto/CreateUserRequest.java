package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to create a new user by admin")
public record CreateUserRequest(
        @Schema(description = "Unique login (student number or teacher code)", example = "s102344")
        @NotBlank(message = "username must not be blank") String username, // Уникальный идентификато - либо номер студенчекого либо номер у препода
        @Schema(description = "User email", example = "student.petrov@university.edu")
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a well-formed email address")
        String email,
        @Schema(description = "First name", example = "Ivan")
        @NotBlank(message = "firstName must not be blank") String firstName,
        @Schema(description = "Last name", example = "Petrov")
        @NotBlank(message = "lastName must not be blank") String lastName,
        @Schema(description = "Middle name", example = "Sergeevich")
        String middleName,
        @Schema(description = "Primary user role", example = "ROLE_STUDENT")
        @NotNull(message = "role is required") RoleName role,
        @Schema(description = "Target student group id (required for students)", example = "101")
        Long groupId,
        @Schema(description = "Target department id (required for teachers)", example = "12")
        Long departmentId
) {}
