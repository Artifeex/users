package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Search filters for admin users endpoint")
public record UserSearchFilter(
        @Schema(description = "Filter by role name", example = "ROLE_STUDENT")
        String role,
        @Schema(description = "Filter by first name", example = "Ivan")
        String firstName,
        @Schema(description = "Filter by last name", example = "Petrov")
        String lastName,
        @Schema(description = "Filter by email", example = "petrov@university.edu")
        String email,
        @Schema(description = "Filter by active status", example = "true")
        Boolean active
) {}
