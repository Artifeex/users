package ru.sandr.users.user.teacheraccess.dto;

import ru.sandr.users.user.teacheraccess.entity.TeacherGroupAccessScopeType;

public record TeacherGroupAccessScopeResponse(
        TeacherGroupAccessScopeType scopeType,
        Long scopeId
) {
}
