package ru.sandr.users.hierarchy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateStudentGroupRequest(
        @NotBlank
        @Size(max = 50, message = "Название группы не может быть больше 50 символов")
        String name,
        @NotNull Long facultyId,
        @NotNull Long fieldOfStudyId
) {}
