package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import ru.sandr.users.core.validation.OptionalNotBlank;

@Schema(description = "Partial update payload for student group")
public record UpdateStudentGroupRequest(
        @Schema(description = "Updated group name", example = "SE-23-02")
        @OptionalNotBlank(message = "name must not be blank")
        @Size(max = 50, message = "name must be at most 50 characters")
        String name,
        @Schema(description = "Updated faculty id", example = "2")
        @OptionalNotBlank(message = "facultyId must not be blank when provided") Long facultyId,
        @Schema(description = "Updated field of study id", example = "17")
        @OptionalNotBlank(message = "fieldOfStudyId must not be blank when provided") Long fieldOfStudyId
) {}
