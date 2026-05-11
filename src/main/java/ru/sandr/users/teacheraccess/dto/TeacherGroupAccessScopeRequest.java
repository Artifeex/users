package ru.sandr.users.teacheraccess.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sandr.users.teacheraccess.entity.TeacherGroupAccessScopeType;

@Schema(description = "Request payload to grant teacher access scope")
public record TeacherGroupAccessScopeRequest(
        @Schema(description = "Scope type level in hierarchy", example = "FACULTY")
        @NotNull(message = "scopeType is required") TeacherGroupAccessScopeType scopeType,
        @Schema(description = "Target hierarchy node id for selected scope type", example = "1")
        @NotNull(message = "scopeId is required") Long scopeId
) {
}
