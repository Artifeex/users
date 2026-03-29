package ru.sandr.users.user.events;

import lombok.Builder;
import ru.sandr.users.core.events.DomainEvent;

import java.util.UUID;

@Builder
public record UserCreatedEvent (
    UUID userId,
    String email,
    String username,
    String firstName,
    String lastName,
    String temporaryPassword
) implements DomainEvent {

    @Override
    public String getAggregateId() {
        return "";
    }

    @Override
    public String getType() {
        return "UserCreatedEvent";
    }

    @Override
    public String getAggregateType() {
        return "User";
    }
}
