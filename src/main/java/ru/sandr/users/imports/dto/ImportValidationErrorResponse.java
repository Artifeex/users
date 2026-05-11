package ru.sandr.users.imports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Validation error details for import requests")
public record ImportValidationErrorResponse(
        @Schema(description = "Human-readable import validation summary", example = "Found invalid values in uploaded file")
        String message,
        @Schema(description = "Detailed row-level validation errors")
        List<ImportRowError> errors
) {}
