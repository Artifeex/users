package ru.sandr.users.teacheraccess.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;

@Schema(description = "Granted teacher access scope with resolved hierarchy node name")
public record TeacherGroupAccessScopeDetailsResponse(
        @Schema(description = "Target hierarchy node id", example = "1")
        Long scopeId,
        @Schema(description = "Scope type level in hierarchy", example = "FACULTY")
        TeacherGroupAccessScopeType scopeType,
        @Schema(description = "Resolved hierarchy node name", example = "Faculty of Computer Science")
        String scopeName
) {
}
