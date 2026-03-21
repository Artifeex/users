package ru.sandr.users.hierarchy.dto;

public record FieldOfStudyResponse(
        Long id,
        String code,
        String name,
        Long facultyId
) {}
