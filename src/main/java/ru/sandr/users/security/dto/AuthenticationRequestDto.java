package ru.sandr.users.security.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequestDto(
        @NotBlank(message = "Не передан обязательный параметр login")
        String login,
        @NotBlank(message = "Не передан обязательный параметр password")
        String password
) { }
