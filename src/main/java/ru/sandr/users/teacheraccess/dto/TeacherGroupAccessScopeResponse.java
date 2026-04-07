package ru.sandr.users.teacheraccess.dto;

import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;

public record TeacherGroupAccessScopeResponse(
        TeacherGroupAccessScopeType scopeType,
        Long scopeId
) {
}
