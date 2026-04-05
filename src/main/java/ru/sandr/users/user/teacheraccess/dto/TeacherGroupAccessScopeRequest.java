package ru.sandr.users.user.teacheraccess.dto;

import jakarta.validation.constraints.NotNull;
import ru.sandr.users.user.teacheraccess.entity.TeacherGroupAccessScopeType;

public record TeacherGroupAccessScopeRequest(
        @NotNull TeacherGroupAccessScopeType scopeType,
        @NotNull Long scopeId
) {
}
