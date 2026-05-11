package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to create student group")
public record CreateStudentGroupRequest(
        @Schema(description = "Student group name", example = "SE-23-01")
        @NotBlank(message = "name must not be blank")
        @Size(max = 50, message = "name must be at most 50 characters")
        String name,
        @Schema(description = "Parent faculty id", example = "1")
        @NotNull(message = "facultyId is required") Long facultyId,
        @Schema(description = "Parent field of study id", example = "15")
        @NotNull(message = "fieldOfStudyId is required") Long fieldOfStudyId
) {}
