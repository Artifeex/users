package ru.sandr.users.teacheraccess.dto;

import java.util.List;
import java.util.UUID;

public record TeacherGroupAccessResponse(
        UUID teacherId,
        List<TeacherGroupAccessScopeResponse> scopes
) {
}
