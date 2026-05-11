package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Student group representation")
public record StudentGroupResponse(
        @Schema(description = "Student group id", example = "101")
        Long id,
        @Schema(description = "Student group name", example = "SE-23-01")
        String name,
        @Schema(description = "Parent faculty id", example = "1")
        Long facultyId,
        @Schema(description = "Parent field of study id", example = "15")
        Long fieldOfStudyId
) {}
