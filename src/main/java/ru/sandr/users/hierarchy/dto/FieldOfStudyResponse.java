package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Field of study representation")
public record FieldOfStudyResponse(
        @Schema(description = "Field of study id", example = "15")
        Long id,
        @Schema(description = "Field of study name", example = "Software Engineering")
        String name,
        @Schema(description = "Parent faculty id", example = "1")
        Long facultyId
) {}
