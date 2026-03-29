package ru.sandr.users.hierarchy.dto;

public record FieldOfStudyResponse(
        Long id,
        String name,
        Long facultyId
) {}
