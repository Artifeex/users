package ru.sandr.users.hierarchy.dto;

import jakarta.validation.constraints.Size;
import ru.sandr.users.core.validation.OptionalNotBlank;

public record UpdateFacultyRequest(
        @OptionalNotBlank
        String name,
        @OptionalNotBlank
        @Size(max = 50, message = "Короткое название факультета больше 50 символов")
        String shortName
) {}
