package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to update profile data of authenticated user")
public record UpdateOwnProfileRequest(
        @Schema(description = "New email address", example = "teacher.ivanov@university.edu")
        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a well-formed email address")
        String email
) {}
