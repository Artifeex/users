package ru.sandr.users.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authenticated user profile with role-specific fields")
public record CurrentUserProfileResponse(
        @Schema(description = "User unique identifier", example = "4b4f60a5-7b67-44f3-9e5a-9df45f77f653")
        UUID id,
        @Schema(description = "Assigned role names", example = "[\"ROLE_STUDENT\"]")
        List<String> roles,
        @Schema(description = "First name", example = "Ivan")
        String firstName,
        @Schema(description = "Last name", example = "Petrov")
        String lastName,
        @Schema(description = "Middle name", example = "Sergeevich")
        String middleName,
        @Schema(description = "Email (admin)", example = "admin@university.edu")
        String email,
        @Schema(description = "Avatar file id in file-service", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID avatarFileId,
        @Schema(description = "Faculty (institute) name", example = "Faculty of Computer Science")
        String faculty,
        @Schema(description = "Field of study name (student)", example = "Software Engineering")
        String fieldOfStudy,
        @Schema(description = "Department name", example = "Department of Applied Mathematics")
        String department,
        @Schema(description = "Student group name", example = "SE-23-01")
        String studentGroup
) {}
