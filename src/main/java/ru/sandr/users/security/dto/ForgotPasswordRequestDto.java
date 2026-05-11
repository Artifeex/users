package ru.sandr.users.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to initiate password recovery flow")
public record ForgotPasswordRequestDto(
        @Schema(description = "User email for reset instructions", example = "student.petrov@university.edu")
        @Email(message = "email must be a well-formed email address")
        @NotBlank(message = "email must not be blank")
        String email
) {
}
