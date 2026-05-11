package ru.sandr.users.teacheraccess.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReplaceTeacherGroupAccessRequest(
        @NotNull(message = "scopes is required") List<@Valid TeacherGroupAccessScopeRequest> scopes
) {
}
