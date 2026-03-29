package ru.sandr.users.core.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.sandr.users.core.entity.OutboxMessage;
import ru.sandr.users.core.enums.OutboxStatus;
import ru.sandr.users.core.events.DomainEvent;
import ru.sandr.users.core.repository.OutboxMessageRepository;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventListener {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleDomainEvent(DomainEvent domainEvent) {
        try {
            OutboxMessage outboxMessage = OutboxMessage.builder()
                                                       .aggregateId(domainEvent.getAggregateId())
                                                       .aggregateType(domainEvent.getAggregateType())
                                                       .type(domainEvent.getType())
                                                       .payload(objectMapper.writeValueAsString(domainEvent))
                                                       .status(OutboxStatus.NEW)
                                                       .createdAt(Instant.now())
                                                       .build();
            outboxMessageRepository.save(outboxMessage);
            log.info("Saved outbox event with type: {}", domainEvent.getType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize json for message: {}", domainEvent, e);
            throw new RuntimeException("Failed to serialize json for message: " + domainEvent, e);
        }
    }
}
