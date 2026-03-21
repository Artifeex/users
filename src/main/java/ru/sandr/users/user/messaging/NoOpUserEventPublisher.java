package ru.sandr.users.user.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sandr.users.user.messaging.event.UserCreatedEvent;

/**
 * Преобразовать эту реализацию на
 */
@Slf4j
@Service
public class NoOpUserEventPublisher implements UserEventPublisher {

    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        log.info("[NoOp] User created event for userId={}, username={}", event.userId(), event.username());
    }
}
