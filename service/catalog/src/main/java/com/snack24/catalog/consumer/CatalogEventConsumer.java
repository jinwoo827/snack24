package com.snack24.catalog.consumer;

import com.snack24.catalog.repository.StockRepository;
import com.snack24.catalog.service.StockCommandHandler;
import com.snack24.common.event.Event;
import com.snack24.common.event.EventPayload;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.ReleaseStockCommandPayload;
import com.snack24.common.event.payload.ReserveStockCommandPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogEventConsumer {
    private final StockCommandHandler stockCommandHandler;

    @KafkaListener(
            topics = EventType.Topic.CATALOG
    )
    public void listen(String message, Acknowledgment ack) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event == null) {
            ack.acknowledge();
            return;
        }

        switch (event.getType()) {
            case EventType.RESERVE_STOCK_COMMAND -> stockCommandHandler.handleReserve((ReserveStockCommandPayload) event.getPayload());
            case EventType.RELEASE_STOCK_COMMAND -> stockCommandHandler.handleRelease((ReleaseStockCommandPayload) event.getPayload());
            default -> {}
        }
        
        ack.acknowledge();
    }
}
