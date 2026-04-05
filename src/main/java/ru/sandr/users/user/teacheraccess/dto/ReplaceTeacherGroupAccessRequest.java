package ru.sandr.users.user.teacheraccess.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReplaceTeacherGroupAccessRequest(
        @NotNull List<@Valid TeacherGroupAccessScopeRequest> scopes
) {
}
