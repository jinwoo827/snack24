package com.snack24.order.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "ordered_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime orderedAt;

    @Column(name = "confirmed_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime confirmedAt;

    @Column(name = "canceled_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason", length = 100)
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(Long orderId, Long companyId, Long memberId) {
        Order o = new Order();
        o.orderId = orderId;
        o.companyId = companyId;
        o.memberId = memberId;
        o.status = OrderStatus.PENDING;
        o.totalAmount = BigDecimal.ZERO;
        o.orderedAt = LocalDateTime.now();
        return o;
    }

    public void addItems(OrderItem item) {
        items.add(item);
        item.assignOrder(this);
        this.totalAmount = this.totalAmount.add(item.getLineTotal());
    }

    public void confirm() {
        ensurePending();
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        ensurePending();
        this.status = OrderStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
        this.cancelReason = reason;
    }

    private void ensurePending() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("해당 주문은 대기상태가 아닙니다.");
        }
    }


}
