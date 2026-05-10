package ru.sandr.users.user.events;

import lombok.Builder;
import ru.sandr.users.core.events.DomainEvent;

import java.util.UUID;

@Builder
public record PasswordChangedEvent(
        UUID userId,
        String email
) implements DomainEvent {

    public static final String TYPE_NAME = "PasswordChangedEvent";

    @Override
    public String getAggregateId() {
        return userId.toString();
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public String getAggregateType() {
        return "User";
    }
}
