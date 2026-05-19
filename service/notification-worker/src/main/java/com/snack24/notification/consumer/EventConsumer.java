package com.snack24.notification.consumer;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventPayload;
import com.snack24.common.event.EventType;
import com.snack24.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = {
                    EventType.Topic.IDENTITY
            }
    )
    public void listen(String message, Acknowledgment ack) {
        log.info("[EventConsumer.listen] message = {}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            notificationService.handle(event);
        }
        ack.acknowledge();
    }
}
