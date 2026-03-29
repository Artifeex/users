package ru.sandr.users.core.events;

public interface DomainEvent {
    String getAggregateId();
    String getAggregateType();
    String getType();
}
