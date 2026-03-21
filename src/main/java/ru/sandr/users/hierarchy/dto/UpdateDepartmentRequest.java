package ru.sandr.users.hierarchy.dto;

import ru.sandr.users.core.validation.OptionalNotBlank;

public record UpdateDepartmentRequest(
        @OptionalNotBlank String name,
        @OptionalNotBlank Long facultyId
) {}
