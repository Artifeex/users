package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sandr.users.core.validation.OptionalNotBlank;

@Schema(description = "Partial update payload for department")
public record UpdateDepartmentRequest(
        @Schema(description = "Updated department name", example = "Department of Theoretical Physics")
        @OptionalNotBlank(message = "name must not be blank") String name,
        @Schema(description = "Updated parent faculty id", example = "2")
        @OptionalNotBlank(message = "facultyId must not be blank when provided") Long facultyId
) {}
