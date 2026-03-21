package ru.sandr.users.hierarchy.dto;

public record DepartmentResponse(
        Long id,
        String name,
        Long facultyId
) {}
