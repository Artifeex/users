package ru.sandr.users.hierarchy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDepartmentRequest(
        @NotBlank String name,
        @NotNull Long facultyId
) {}
