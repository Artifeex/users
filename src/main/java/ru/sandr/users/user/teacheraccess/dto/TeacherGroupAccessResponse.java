package ru.sandr.users.user.teacheraccess.dto;

import java.util.List;
import java.util.UUID;

public record TeacherGroupAccessResponse(
        UUID teacherId,
        List<TeacherGroupAccessScopeResponse> scopes
) {
}
