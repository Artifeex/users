package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to set avatar file id for current user")
public record ChangeAvatarRequestDto(
        @Schema(description = "File id returned by file-service upload flow", example = "4b4f60a5-7b67-44f3-9e5a-9df45f77f653")
        @NotBlank(message = "fileId must not be blank") String fileId
) {
}
