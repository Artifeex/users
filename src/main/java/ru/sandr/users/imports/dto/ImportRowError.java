package ru.sandr.users.imports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single import row validation error")
public record ImportRowError(
        @Schema(description = "Excel row number", example = "12")
        int row,
        @Schema(description = "Column name where validation failed", example = "email")
        String column,
        @Schema(description = "Validation error message", example = "Email format is invalid")
        String message
) {}
