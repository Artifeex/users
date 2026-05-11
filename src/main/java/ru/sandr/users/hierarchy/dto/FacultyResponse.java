package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Faculty representation")
public record FacultyResponse(
        @Schema(description = "Faculty id", example = "1")
        Long id,
        @Schema(description = "Faculty full name", example = "Faculty of Computer Science")
        String name,
        @Schema(description = "Faculty short name", example = "FCS")
        String shortName
) {}
