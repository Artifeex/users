package ru.sandr.users.hierarchy.dto;

import jakarta.validation.constraints.Size;
import ru.sandr.users.core.validation.OptionalNotBlank;

public record UpdateStudentGroupRequest(
        @OptionalNotBlank
        @Size(max = 50, message = "Имя группы не может быть больше 50 символов")
        String name,
        @OptionalNotBlank Long facultyId,
        @OptionalNotBlank Long fieldOfStudyId
) {}
