package ru.sandr.users.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminUserDetailsResponse(
        String firstName,
        String middleName,
        String lastName,
        String email,
        List<String> roles,
        String username,
        String faculty,
        String fieldOfStudy,
        String studentGroup,
        String department
) {}
