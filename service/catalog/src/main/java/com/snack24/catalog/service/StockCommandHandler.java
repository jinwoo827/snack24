package com.snack24.catalog.service;

import com.snack24.catalog.domain.Stock;
import com.snack24.catalog.repository.StockRepository;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.*;
import com.snack24.common.outboxrelay.outbox.event.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockCommandHandler {
    private final StockRepository stockRepository;
    private final ProcessedMessageService processedMessageService;
    private final OutboxEventPublisher outboxEventPublisher;

    // 재고 예약
    @Transactional
    public void handleReserve(ReserveStockCommandPayload payload) {
        // 재고 예약 이벤트 멱등성 체크
        if (!processedMessageService.markIfFirst(payload.getSagaId(), EventType.RESERVE_STOCK_COMMAND)) {
            return;
        }

        List<ReserveStockCommandPayload.Item> sortedItems = payload.getItems().stream()
                .sorted(Comparator.comparing(ReserveStockCommandPayload.Item::getProductId))
                .toList();

        List<Long> productIds = sortedItems.stream()
                .map(ReserveStockCommandPayload.Item::getProductId)
                .toList();

        // 비관적 락 적용
        Map<Long, Stock> stockMap = stockRepository.findAllByProductIdInForUpdate(productIds)
                .stream()
                .collect(Collectors.toMap(Stock::getProductId, Function.identity()));

        // 수량 가능 체크
        String failReason = checkAvailability(sortedItems, stockMap);

        if (failReason == null) {
            sortedItems.forEach(item -> {
                stockMap.get(item.getProductId()).reserve(item.getQuantity());
            });

            // 재고 예약 완료 이벤트
            publishReserved(payload);
        } else {
            publishReservationFailed(payload, failReason);
        }
    }

    // 재고 반환
    @Transactional
    public void handleRelease(ReleaseStockCommandPayload payload) {
        if (!processedMessageService.markIfFirst(payload.getSagaId(), EventType.RELEASE_STOCK_COMMAND)) {
            return;
        }

        List<ReserveStockCommandPayload.Item> sortedItems = payload.getItems().stream()
                .sorted(Comparator.comparing(ReserveStockCommandPayload.Item::getProductId))
                .toList();

        List<Long> productIds = sortedItems.stream()
                .map(ReserveStockCommandPayload.Item::getProductId)
                .toList();

        Map<Long, Stock> stockMap = stockRepository.findAllByProductIdInForUpdate(productIds)
                .stream()
                .collect(Collectors.toMap(Stock::getProductId, Function.identity()));

        sortedItems.forEach(item -> {
            stockMap.get(item.getProductId()).release(item.getQuantity());
        });

        // 재고 반환 이벤트
        publishReleased(payload);

    }

    private void publishReserved(ReserveStockCommandPayload payload) {
        outboxEventPublisher.publish(
                EventType.STOCK_RESERVED,
                StockReservedPayload.builder()
                        .sagaId(payload.getSagaId())
                        .orderId(payload.getOrderId())
                        .build(),
                payload.getSagaId()
        );
    }

    private void publishReservationFailed(ReserveStockCommandPayload payload, String reason) {
        outboxEventPublisher.publish(
                EventType.STOCK_RESERVATION_FAILED,
                StockReservationFailedPayload.builder()
                        .sagaId(payload.getSagaId())
                        .orderId(payload.getOrderId())
                        .reason(reason)
                        .build(),
                payload.getSagaId()
        );
    }

    private void publishReleased(ReleaseStockCommandPayload payload) {
        outboxEventPublisher.publish(
                EventType.STOCK_RELEASED,
                StockReleasedPayload.builder()
                        .sagaId(payload.getSagaId())
                        .orderId(payload.getOrderId())
                        .build(),
                payload.getSagaId()
        );
    }

    private String checkAvailability(List<ReserveStockCommandPayload.Item> items, Map<Long, Stock> stockMap) {
        for (ReserveStockCommandPayload.Item reqItem : items) {
            Stock stock = stockMap.get(reqItem.getProductId());
            if (stock == null) {
                return "product not found productId : " + stock.getProductId();
            }

            if (reqItem.getQuantity() > stock.getAvailableQty()) {
                return "out of stock : productId = %s, request quantity = %s, available = %s".formatted(stock.getProductId(), reqItem.getQuantity(), stock.getAvailableQty());
            }
        }
        return null;
    }
}
