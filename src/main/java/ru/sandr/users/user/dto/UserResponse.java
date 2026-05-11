package ru.sandr.users.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Public user representation used in user management endpoints")
public record UserResponse(
        @Schema(description = "User unique identifier", example = "4b4f60a5-7b67-44f3-9e5a-9df45f77f653")
        UUID id,
        @Schema(description = "Login name", example = "s102344")
        String username,
        @Schema(description = "Email address", example = "student.petrov@university.edu")
        String email,
        @Schema(description = "First name", example = "Ivan")
        String firstName,
        @Schema(description = "Last name", example = "Petrov")
        String lastName,
        @Schema(description = "Middle name", example = "Sergeevich")
        String middleName,
        @Schema(description = "Is user active", example = "true")
        boolean active,
        @Schema(description = "Assigned role names", example = "[\"ROLE_STUDENT\"]")
        Set<String> roles
) {}
