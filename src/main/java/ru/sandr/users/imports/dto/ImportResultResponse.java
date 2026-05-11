package ru.sandr.users.imports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Import summary response")
public record ImportResultResponse(
        @Schema(description = "Number of successfully imported rows", example = "245")
        int imported
) {}
