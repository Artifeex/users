package ru.sandr.users.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.config.RouterConfig;
import ru.sandr.users.core.entity.OutboxMessage;
import ru.sandr.users.core.enums.OutboxStatus;
import ru.sandr.users.core.repository.OutboxMessageRepository;
import ru.sandr.users.user.events.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxSenderService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;
    private final RouterConfig routerConfig;

    //@Scheduled(fixedDelayString = "${outbox.poll-interval:2000}")
    @SchedulerLock(name = "OutboxSenderService_sendOutboxMessages",
            lockAtLeastFor = "1s", // Для предотвращения потенциального рассинхронна по времени. Когда задача будет завершена, то при установке lock_until, если задача была завершена быстрее времени, указанного в lockAtLeastFor, то запишется NOW() + lockAtLeastFor
            lockAtMostFor = "5m" // На случай падений пода, чтобы блокировка рано или поздно освободилась
    )
    @Transactional
    public void sendOutboxMessages() {
        var messagesToSend = outboxMessageRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);
        if(CollectionUtils.isEmpty(messagesToSend)) {
            return;
        }
        List<CompletableFuture<SendResult<String, String>>> futures = new ArrayList<>();
        for (OutboxMessage outboxMessage : messagesToSend) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                    getTopicNameByEventName(outboxMessage.getType()),
                    null,
                    outboxMessage.getAggregateId(),
                    outboxMessage.getPayload()
            );
            producerRecord.headers().add("event_type", outboxMessage.getType().getBytes(StandardCharsets.UTF_8));
            producerRecord.headers().add("message_id", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            futures.add(outboxKafkaTemplate.send(producerRecord));
            outboxMessage.setStatus(OutboxStatus.SENT);
        }

        try {
            // join - ожидаем окончания выполнения всех future
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.info("Messages sent successfully");
        } catch (Exception e) {
            // Отправка как минимум одного из сообщений завершилась с ошибкой
            // Логируем и откатываем БД
            log.error("Failed to send messages from outbox", e);
            throw new RuntimeException(e);
        }
    }

    private String getTopicNameByEventName(String eventName) {
        return switch (eventName) {
            case FileLoadedEvent.TYPE_NAME, FileDeletedEvent.TYPE_NAME -> routerConfig.getFileTopicName();
            case UserCreatedEvent.TYPE_NAME, PasswordChangedEvent.TYPE_NAME, ResetPasswordEvent.TYPE_NAME ->
                    routerConfig.getUserTopicName();
            default -> throw new IllegalStateException("Unexpected value: " + eventName);
        };
    }
}
