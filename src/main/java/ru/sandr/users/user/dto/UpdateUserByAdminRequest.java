package ru.sandr.users.user.dto;

import jakarta.validation.constraints.Email;
import ru.sandr.users.core.validation.OptionalNotBlank;
import ru.sandr.users.user.entity.Role;

import java.util.Set;

public record UpdateUserByAdminRequest(
        @Email String email,
        @OptionalNotBlank String username,
        @OptionalNotBlank String firstName,
        @OptionalNotBlank String lastName,
        @OptionalNotBlank String middleName,
        Long groupId,
        Long departmentId,
        Set<RoleName> roles
) {}
