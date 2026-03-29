package ru.sandr.users.user.events;

import lombok.Builder;
import ru.sandr.users.core.events.DomainEvent;

import java.util.UUID;

@Builder
public record ResetPasswordEvent(
        UUID userId,
        String email,
        String linkForResetEvent
) implements DomainEvent {

    @Override
    public String getAggregateId() {
        return userId.toString();
    }

    @Override
    public String getType() {
        return "ResetPasswordEvent";
    }

    @Override
    public String getAggregateType() {
        return "User";
    }
}
