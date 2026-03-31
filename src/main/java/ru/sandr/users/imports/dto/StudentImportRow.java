package ru.sandr.users.imports.dto;

public record StudentImportRow(
        String username,
        String email,
        String firstName,
        String lastName,
        String middleName,
        boolean active,
        Long groupId,
        Long departmentId
) {}
