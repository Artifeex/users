package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ru.sandr.users.core.validation.OptionalNotBlank;

@Schema(description = "Partial update payload for faculty")
public record UpdateFacultyRequest(
        @Schema(description = "Updated faculty full name", example = "Faculty of Applied Informatics")
        @OptionalNotBlank(message = "name must not be blank")
        String name,
        @Schema(description = "Updated faculty short name", example = "FAI")
        @OptionalNotBlank(message = "shortName must not be blank")
        @Size(max = 50, message = "shortName must be at most 50 characters")
        String shortName
) {}
