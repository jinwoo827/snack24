package com.snack24.order.service;

import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.ReserveStockCommandPayload;
import com.snack24.common.outboxrelay.outbox.event.OutboxEventPublisher;
import com.snack24.common.snowflake.Snowflake;
import com.snack24.order.client.CatalogClient;
import com.snack24.order.client.dto.ProductPrice;
import com.snack24.order.domain.Order;
import com.snack24.order.domain.OrderItem;
import com.snack24.order.domain.SagaInstance;
import com.snack24.order.exception.OrderErrorCode;
import com.snack24.order.exception.OrderException;
import com.snack24.order.repository.OrderRepository;
import com.snack24.order.repository.SagaInstanceRepository;
import com.snack24.order.service.request.OrderPlaceRequest;
import com.snack24.order.service.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@ToString
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final Snowflake snowflake;
    private final CatalogClient catalogClient;
    private final SagaInstanceRepository sagaRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public Long place(Long companyId, Long memberId, String role, OrderPlaceRequest request) {
        Long orderId = snowflake.nextId();
        Order order = Order.create(orderId, companyId, memberId);

        for (OrderPlaceRequest.Item item : request.items()) {
            ProductPrice price = catalogClient.getPrice(item.productId(), companyId, memberId, role);
            order.addItems(
                    OrderItem.create(
                            snowflake.nextId(),
                            item.productId(),
                            price.name(),
                            price.unitPrice(),
                            item.quantity()
                    )
            );
        }
        orderRepository.save(order);

        // saga
        Long sagaId = snowflake.nextId();
        sagaRepository.save(SagaInstance.startOrderPlacement(sagaId, orderId));



        // 재고 예약 이벤트
        List<ReserveStockCommandPayload.Item> items = order.getItems().stream()
                        .map(item -> ReserveStockCommandPayload.Item.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build()
                        )
                        .toList();

        outboxEventPublisher.publish(
                EventType.RESERVE_STOCK_COMMAND,
                ReserveStockCommandPayload.builder()
                        .sagaId(sagaId)
                        .companyId(companyId)
                        .orderId(orderId)
                        .items(items)
                        .build(),
                sagaId
        );
        return orderId;
    }

    public OrderResponse get(Long orderId, Long callerCompanyId) {
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getCompanyId().equals(callerCompanyId))
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResponse.from(order);
    }
}
