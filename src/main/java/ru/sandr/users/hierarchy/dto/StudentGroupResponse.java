package ru.sandr.users.hierarchy.dto;

public record StudentGroupResponse(
        Long id,
        String name,
        Long facultyId,
        Long fieldOfStudyId
) {}
