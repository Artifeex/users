package ru.sandr.users.hierarchy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFacultyRequest(
        @NotBlank String name,
        @NotBlank @Size(max = 50) String shortName
) {}
