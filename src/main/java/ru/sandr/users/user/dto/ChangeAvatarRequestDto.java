package ru.sandr.users.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeAvatarRequestDto(
        @NotBlank String fileId
) {
}
