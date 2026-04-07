package ru.sandr.users.teacheraccess.dto;

import jakarta.validation.constraints.NotNull;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;

public record TeacherGroupAccessScopeRequest(
        @NotNull TeacherGroupAccessScopeType scopeType,
        @NotNull Long scopeId
) {
}
