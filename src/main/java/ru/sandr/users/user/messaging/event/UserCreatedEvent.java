package ru.sandr.users.user.messaging.event;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email,
        String username,
        String temporaryPassword
) {}
