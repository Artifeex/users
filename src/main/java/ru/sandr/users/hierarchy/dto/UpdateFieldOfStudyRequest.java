package ru.sandr.users.hierarchy.dto;

import ru.sandr.users.core.validation.OptionalNotBlank;

public record UpdateFieldOfStudyRequest(
        @OptionalNotBlank String name,
        Long facultyId
) {}
