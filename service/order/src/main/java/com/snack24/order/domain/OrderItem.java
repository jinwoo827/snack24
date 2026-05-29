package com.snack24.order.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

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

    public static OrderItem create(Long orderItemId, Long productId, String productName, BigDecimal unitPriceAtOrder, int quantity) {
        OrderItem i = new OrderItem();
        i.orderItemId = orderItemId;
        i.productId = productId;
        i.productName = productName;
        i.unitPriceAtOrder = unitPriceAtOrder;
        i.quantity = quantity;
        i.lineTotal = unitPriceAtOrder.multiply(BigDecimal.valueOf(quantity));
        return i;
    }

    void assignOrder(Order order) {
        this.order = order;
    }
}
