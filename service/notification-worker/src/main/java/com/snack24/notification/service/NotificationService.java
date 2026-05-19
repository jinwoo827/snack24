package com.snack24.notification.service;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventPayload;
import com.snack24.notification.handler.EventHandler;
import com.snack24.notification.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final List<EventHandler> eventHandlers;
    private final ProcessedEventRepository processedEventRepository;

    public void handle(Event<EventPayload> event) {
        // 멱등성 체크
        if (!processedEventRepository.markIfFirst(event.getEventId())) {
            log.info("[Notification] duplicate event skipped. eventId = {}", event.getEventId());
            return;
        }

        // 핸들러 이벤트 처리
        eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .ifPresentOrElse(
                        eventHandler -> eventHandler.handle(event),
                        () -> log.warn("[Notification] no handler for type = {}", event.getType())
                );
    }
}
