package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import ru.sandr.users.core.validation.OptionalNotBlank;

import java.util.Set;

@Schema(description = "Partial update payload for admin user management")
public record UpdateUserByAdminRequest(
        @Schema(description = "Updated email", example = "new.email@university.edu")
        @Email(message = "email must be a well-formed email address") String email,
        @Schema(description = "Updated login", example = "t88321")
        @OptionalNotBlank(message = "username must not be blank") String username,
        @Schema(description = "Updated first name", example = "Petr")
        @OptionalNotBlank(message = "firstName must not be blank") String firstName,
        @Schema(description = "Updated last name", example = "Ivanov")
        @OptionalNotBlank(message = "lastName must not be blank") String lastName,
        @Schema(description = "Updated middle name", example = "Alexandrovich")
        @OptionalNotBlank(message = "middleName must not be blank") String middleName,
        @Schema(description = "Updated student group id", example = "202")
        Long groupId,
        @Schema(description = "Updated department id", example = "8")
        Long departmentId,
        @Schema(description = "Updated user roles", example = "[\"ROLE_TEACHER\"]")
        Set<RoleName> roles
) {}
