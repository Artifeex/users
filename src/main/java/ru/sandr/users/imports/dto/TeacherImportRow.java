package ru.sandr.users.imports.dto;

public record TeacherImportRow(
        String username,
        String email,
        String firstName,
        String lastName,
        String middleName,
        boolean active,
        Long departmentId
) {}
