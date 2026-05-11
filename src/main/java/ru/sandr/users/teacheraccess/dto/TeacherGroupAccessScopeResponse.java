package ru.sandr.users.teacheraccess.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;

@Schema(description = "Granted teacher access scope")
public record TeacherGroupAccessScopeResponse(
        @Schema(description = "Scope type level in hierarchy", example = "FACULTY")
        TeacherGroupAccessScopeType scopeType,
        @Schema(description = "Target hierarchy node id", example = "1")
        Long scopeId
) {
}
