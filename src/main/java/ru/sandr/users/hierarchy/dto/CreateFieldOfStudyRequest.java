package ru.sandr.users.hierarchy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFieldOfStudyRequest(
        @NotBlank
        @Size(max = 255)
        String name,
        @NotNull Long facultyId
) {}
