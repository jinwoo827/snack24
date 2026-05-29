package com.snack24.order.service;

import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.*;
import com.snack24.common.outboxrelay.outbox.event.OutboxEventPublisher;
import com.snack24.order.domain.Order;
import com.snack24.order.domain.SagaInstance;
import com.snack24.order.exception.OrderErrorCode;
import com.snack24.order.exception.OrderException;
import com.snack24.order.repository.OrderRepository;
import com.snack24.order.repository.SagaInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SagaOrchestrator {
    private final SagaInstanceRepository sagaInstanceRepository;
    private final OrderRepository orderRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    // 재고 예약 성공 -> 잔액 차감 이벤트 발행
    @Transactional
    public void onStockReserved(StockReservedPayload payload) {
        SagaInstance saga = load(payload.getSagaId());
        saga.markStockReserved();
        Order order = orderRepository.findById(payload.getOrderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        outboxEventPublisher.publish(
                EventType.DEBIT_WALLET_COMMAND,
                DebitWalletCommandPayload.builder()
                        .sagaId(saga.getSagaId())
                        .orderId(order.getOrderId())
                        .companyId(order.getCompanyId())
                        .amount(order.getTotalAmount())
                        .build(),
                saga.getSagaId()
        );
    }

    // 재고 예약 실패 -> 주문 취소
    @Transactional
    public void onStockReservationFailed(StockReservationFailedPayload payload) {
        SagaInstance saga = load(payload.getSagaId());
        saga.markCancel();
        Order order = orderRepository.findById(payload.getOrderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        order.cancel(payload.getReason());
        publishOrderCanceled(order, payload.getReason());
    }

    // 잔액 차감 성공 -> 주문 확정
    @Transactional
    public void onWalletDebited(WalletDebitedPayload payload) {
        SagaInstance saga = load(payload.getSagaId());
        saga.markWalletDebited();
        Order order = orderRepository.findById(payload.getOrderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        saga.markConfirmed();
        order.confirm();

        outboxEventPublisher.publish(
                EventType.ORDER_CONFIRMED,
                OrderConfirmedPayload.builder()
                        .orderId(order.getOrderId())
                        .companyId(order.getCompanyId())
                        .memberId(order.getMemberId())
                        .totalAmount(order.getTotalAmount())
                        .confirmedAt(order.getConfirmedAt())
                        .build(),
                order.getOrderId()
        );
    }

    // 잔액 부족 -> 재고 해제 이벤트
    @Transactional
    public void onWalletDebitFailed(WalletDebitFailedPayload payload) {
        SagaInstance saga = load(payload.getSagaId());
        saga.startCompensation(payload.getReason());
        Order order = orderRepository.findById(payload.getOrderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        outboxEventPublisher.publish(
                EventType.RELEASE_STOCK_COMMAND,
                ReleaseStockCommandPayload.builder()
                        .sagaId(saga.getSagaId())
                        .orderId(order.getOrderId())
                        .items(order.getItems().stream().map(item -> ReserveStockCommandPayload.Item.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                                .toList()
                        )
                        .build(),
                saga.getSagaId()
        );
    }

    // 재고 해제 완료 -> 주문 취소
    @Transactional
    public void onStockReleased(StockReleasedPayload payload) {
        SagaInstance saga = load(payload.getSagaId());
        Order order = orderRepository.findById(payload.getOrderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        saga.markCancel();
        order.cancel(saga.getErrorReason());
        publishOrderCanceled(order, saga.getErrorReason());
    }

    private SagaInstance load(Long sagaId) {
        return sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.SAGA_NOT_FOUND));
    }

    private void publishOrderCanceled(Order order, String reason) {
        outboxEventPublisher.publish(
                EventType.ORDER_CANCELED,
                OrderCanceledPayload.builder()
                        .orderId(order.getOrderId())
                        .companyId(order.getCompanyId())
                        .reason(reason)
                        .canceledAt(order.getCanceledAt())
                        .build(),
                order.getOrderId()
        );
    }
}
