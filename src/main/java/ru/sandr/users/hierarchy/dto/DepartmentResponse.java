package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Department representation")
public record DepartmentResponse(
        @Schema(description = "Department id", example = "9")
        Long id,
        @Schema(description = "Department name", example = "Department of Applied Mathematics")
        String name,
        @Schema(description = "Parent faculty id", example = "1")
        Long facultyId
) {}
