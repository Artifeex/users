package ru.sandr.users.user.events;

import lombok.Builder;
import ru.sandr.users.core.events.DomainEvent;

import java.util.UUID;

@Builder
public record PasswordChangedEvent(
        UUID userId,
        String email
) implements DomainEvent {

    @Override
    public String getAggregateId() {
        return userId.toString();
    }

    @Override
    public String getType() {
        return "PasswordChangedEvent";
    }

    @Override
    public String getAggregateType() {
        return "User";
    }
}
