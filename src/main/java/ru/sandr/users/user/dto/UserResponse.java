package ru.sandr.users.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String middleName,
        boolean active,
        Set<String> roles
) {}
