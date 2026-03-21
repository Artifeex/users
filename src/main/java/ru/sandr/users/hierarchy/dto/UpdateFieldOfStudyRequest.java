package ru.sandr.users.hierarchy.dto;

import ru.sandr.users.core.validation.OptionalNotBlank;

public record UpdateFieldOfStudyRequest(
        @OptionalNotBlank String code,
        @OptionalNotBlank String name,
        @OptionalNotBlank Long facultyId
) {}
