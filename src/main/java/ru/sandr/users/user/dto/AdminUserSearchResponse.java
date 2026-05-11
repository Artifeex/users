package ru.sandr.users.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User row projection for admin search results")
public record AdminUserSearchResponse(
        @Schema(description = "User id", example = "4b4f60a5-7b67-44f3-9e5a-9df45f77f653")
        UUID id,
        @Schema(description = "Username/login", example = "t88321")
        String username,
        @Schema(description = "Email", example = "teacher.ivanov@university.edu")
        String email,
        @Schema(description = "First name", example = "Petr")
        String firstName,
        @Schema(description = "Last name", example = "Ivanov")
        String lastName,
        @Schema(description = "Middle name", example = "Alexandrovich")
        String middleName,
        @Schema(description = "Is user active", example = "true")
        boolean active,
        @Schema(description = "Assigned role names", example = "[\"ROLE_TEACHER\"]")
        Set<String> roles,
        @Schema(description = "Department name for teacher users", example = "Department of Physics")
        String department
) {}
