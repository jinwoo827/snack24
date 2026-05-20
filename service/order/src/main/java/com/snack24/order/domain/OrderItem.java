package com.snack24.order.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "order_items")
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {
    @Id
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "unit_price_at_order", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceAtOrder;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;   // unitPrice * quantity

    public static OrderItem create(Long orderItemId, Long orderId, Long productId, String productName, BigDecimal unitPriceAtOrder, int quantity) {
        OrderItem i = new OrderItem();
        i.orderItemId = orderItemId;
        i.orderId = orderId;
        i.productId = productId;
        i.productName = productName;
        i.unitPriceAtOrder = unitPriceAtOrder;
        i.quantity = quantity;
        i.lineTotal = unitPriceAtOrder.multiply(BigDecimal.valueOf(quantity));
        return i;
    }
}
