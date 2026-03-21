package ru.sandr.users.user.dto;

public record UserSearchFilter(
        String role,
        String firstName,
        String lastName,
        String email,
        Boolean active
) {}
