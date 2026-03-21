package ru.sandr.users.user.messaging;

import ru.sandr.users.user.messaging.event.UserCreatedEvent;

public interface UserEventPublisher {

    String TOPIC_USER_CREATED = "users.created";

    /**
     * Выполнить сохранение в outbox таблицу с эвентами и шедулер потом отправить в kafka эвент
     * @param event Создали нового пользователя
     */
    void publishUserCreated(UserCreatedEvent event);
}
