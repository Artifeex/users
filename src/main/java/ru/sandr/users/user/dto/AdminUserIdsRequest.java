package ru.sandr.users.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "List of user ids to load admin details for")
public record AdminUserIdsRequest(
        @Schema(description = "User ids", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
        @NotEmpty(message = "ids must not be empty")
        List<UUID> ids
) {}
