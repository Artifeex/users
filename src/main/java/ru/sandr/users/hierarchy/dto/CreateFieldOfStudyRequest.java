package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to create field of study")
public record CreateFieldOfStudyRequest(
        @Schema(description = "Field of study name", example = "Software Engineering")
        @NotBlank(message = "name must not be blank")
        @Size(max = 255, message = "name must be at most 255 characters")
        String name,
        @Schema(description = "Parent faculty id", example = "1")
        @NotNull(message = "facultyId is required") Long facultyId
) {}
