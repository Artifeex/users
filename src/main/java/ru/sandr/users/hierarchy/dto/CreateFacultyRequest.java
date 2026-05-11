package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to create faculty")
public record CreateFacultyRequest(
        @Schema(description = "Faculty full name", example = "Faculty of Computer Science")
        @NotBlank(message = "name must not be blank") String name,
        @Schema(description = "Faculty short name", example = "FCS")
        @NotBlank(message = "shortName must not be blank")
        @Size(max = 50, message = "shortName must be at most 50 characters")
        String shortName
) {}
