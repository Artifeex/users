package ru.sandr.users.hierarchy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to create department")
public record CreateDepartmentRequest(
        @Schema(description = "Department name", example = "Department of Applied Mathematics")
        @NotBlank(message = "name must not be blank") String name,
        @Schema(description = "Parent faculty id", example = "1")
        @NotNull(message = "facultyId is required") Long facultyId
) {}
