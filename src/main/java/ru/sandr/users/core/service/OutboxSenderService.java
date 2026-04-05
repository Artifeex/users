package ru.sandr.users.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sandr.users.core.entity.OutboxMessage;
import ru.sandr.users.core.enums.OutboxStatus;
import ru.sandr.users.core.repository.OutboxMessageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxSenderService {

    private final OutboxMessageRepository outboxMessageRepository;

    //@Scheduled(fixedDelayString = "${outbox.poll-interval:2000}")
    @SchedulerLock(name = "OutboxSenderService_sendOutboxMessages",
            lockAtLeastFor = "1s", // Для предотвращения потенциального рассинхронна по времени. Когда задача будет завершена, то при установке lock_until, если задача была завершена быстрее времени, указанного в lockAtLeastFor, то запишется NOW() + lockAtLeastFor
            lockAtMostFor = "5m" // На случай падений пода, чтобы блокировка рано или поздно освободилась
    )
    @Transactional
    public void sendOutboxMessages() {
        var messagesToSend = outboxMessageRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);
        for (OutboxMessage outboxMessage : messagesToSend) {
            try {
                // kafkaTemplate.send ... .get() - должны дождаться ack от kafka
                outboxMessage.setStatus(OutboxStatus.SENT);
                log.info("Message with id {} was sent",  outboxMessage.getId());
            } catch (Exception e) {
                log.error("Error while sending outbox message.", e);
                break; // Останавливаем отправку остальных сообщений. Те, что были уже отправлены - закоммитяться
            }
        }
    }
}
