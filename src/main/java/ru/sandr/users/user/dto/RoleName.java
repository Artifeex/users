package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported user roles")
public enum RoleName {
    ROLE_ADMIN,
    ROLE_TEACHER,
    ROLE_STUDENT
}
