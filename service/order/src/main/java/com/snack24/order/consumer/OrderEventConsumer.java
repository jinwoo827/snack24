package com.snack24.order.consumer;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventPayload;
import com.snack24.common.event.EventType;
import com.snack24.common.event.SagaPayload;
import com.snack24.common.event.payload.*;
import com.snack24.order.exception.OrderErrorCode;
import com.snack24.order.exception.OrderException;
import com.snack24.order.service.ProcessedMessageService;
import com.snack24.order.service.SagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {
    private final SagaOrchestrator sagaOrchestrator;
    private final ProcessedMessageService processedMessageService;

    @KafkaListener(
            topics = {
                    EventType.Topic.CATALOG,
                    EventType.Topic.BILLING
            }
    )
    public void listen(String message, Acknowledgment ack) {
        Event<EventPayload> event = Event.fromJson(message);
        log.info("[OrderEventConsumer.listen] event = {}", event);

        if (event == null) {
            ack.acknowledge();
            return;
        }

        switch (event.getType()) {
            case EventType.STOCK_RESERVED -> checkDuplicateAndExecute(event, () -> sagaOrchestrator.onStockReserved((StockReservedPayload) event.getPayload()));
            case EventType.STOCK_RESERVATION_FAILED -> checkDuplicateAndExecute(event, () -> sagaOrchestrator.onStockReservationFailed((StockReservationFailedPayload) event.getPayload()));
            case EventType.WALLET_DEBITED -> checkDuplicateAndExecute(event, (() -> sagaOrchestrator.onWalletDebited((WalletDebitedPayload) event.getPayload())));
            case EventType.WALLET_DEBIT_FAILED -> checkDuplicateAndExecute(event, (() -> sagaOrchestrator.onWalletDebitFailed((WalletDebitFailedPayload) event.getPayload())));
            case EventType.STOCK_RELEASED -> checkDuplicateAndExecute(event, () -> sagaOrchestrator.onStockReleased((StockReleasedPayload) event.getPayload()));
            default -> {}
        }
        ack.acknowledge();
    }

    private void checkDuplicateAndExecute(Event<EventPayload> event, Runnable handler) {
        Long sagaId = extractSagaId(event.getPayload());
        if (!processedMessageService.markIfFirst(sagaId, event.getType())) {
            log.info("[Order] duplicate event skipped. sagaId = {}, type = {}",sagaId, event.getType());
            return;
        }
        handler.run();
    }

    private Long extractSagaId(EventPayload eventPayload) {
        if (eventPayload instanceof SagaPayload sagaPayload) {
            return sagaPayload.getSagaId();
        }
        throw new OrderException(OrderErrorCode.SAGA_NOT_FOUND);
    }
}
