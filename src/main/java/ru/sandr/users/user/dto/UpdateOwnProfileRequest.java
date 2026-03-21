package ru.sandr.users.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateOwnProfileRequest(
        @NotBlank @Email String email
) {}
