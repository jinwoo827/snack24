package com.snack24.order.service.response;

import com.snack24.order.domain.Order;
import com.snack24.order.domain.OrderItem;
import com.snack24.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long companyId,
        Long memberId,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public record OrderItemResponse(
            Long orderItemId,
            Long productId,
            String productName,
            BigDecimal unitPriceAtOrder,
            int quantity,
            BigDecimal lineTotal
    ) {
        public static OrderItemResponse from(OrderItem oi) {
            return new OrderItemResponse(
                    oi.getOrderItemId(),
                    oi.getProductId(),
                    oi.getProductName(),
                    oi.getUnitPriceAtOrder(),
                    oi.getQuantity(),
                    oi.getLineTotal()
            );
        }
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getCompanyId(),
                order.getMemberId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}
