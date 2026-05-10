package ru.sandr.users.user.events;

import ru.sandr.users.core.events.DomainEvent;

import java.util.UUID;

public record FileDeletedEvent(
    UUID fileId
) implements DomainEvent {

    public static final String TYPE_NAME = "FileDeletedEvent";

    @Override
    public String getAggregateId() {
        return fileId.toString();
    }

    @Override
    public String getAggregateType() {
        return "User";
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }
}
