package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sandr.users.core.validation.OptionalNotBlank;

@Schema(description = "Partial update payload for field of study")
public record UpdateFieldOfStudyRequest(
        @Schema(description = "Updated field of study name", example = "Data Science")
        @OptionalNotBlank(message = "name must not be blank") String name,
        @Schema(description = "Updated parent faculty id", example = "2")
        Long facultyId
) {}
