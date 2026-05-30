package ru.sandr.users.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed user profile for admin view")
public record AdminUserDetailsResponse(
        @Schema(description = "User id", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        @Schema(description = "First name", example = "Ivan")
        String firstName,
        @Schema(description = "Middle name", example = "Sergeevich")
        String middleName,
        @Schema(description = "Last name", example = "Petrov")
        String lastName,
        @Schema(description = "Email", example = "ivan.petrov@university.edu")
        String email,
        @Schema(description = "Assigned roles", example = "[\"ROLE_STUDENT\"]")
        List<String> roles,
        @Schema(description = "Username/login", example = "s102344")
        String username,
        @Schema(description = "Faculty name for student scope", example = "Faculty of Computer Science")
        String faculty,
        @Schema(description = "Field of study name for student scope", example = "Software Engineering")
        String fieldOfStudy,
        @Schema(description = "Student group name", example = "SE-23-01")
        String studentGroup,
        @Schema(description = "Department name for teacher scope", example = "Department of Applied Mathematics")
        String department
) {}
