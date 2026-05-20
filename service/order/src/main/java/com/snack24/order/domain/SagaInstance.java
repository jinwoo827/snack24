package com.snack24.order.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@Table(name = "saga_instance")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SagaInstance extends BaseEntity {
    @Id
    @Column(name = "saga_id")
    private Long sagaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_type", nullable = false, length = 30)
    private SagaType sagaType;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SagaStatus status;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "started_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime startedAt;

    @Column(name = "timeout_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime timeoutAt;

    @Column(name = "error_reason", length = 500)
    private String errorReason;

    private static final Duration STEP_TIMEOUT = Duration.ofSeconds(30);

    public static SagaInstance startOrderPlacement(Long sagaId, Long orderId) {
        SagaInstance s = new SagaInstance();
        s.sagaId = sagaId;
        s.sagaType = SagaType.ORDER_PLACEMENT;
        s.orderId = orderId;
        s.status = SagaStatus.STARTED;
        s.currentStep = 1;
        s.startedAt = LocalDateTime.now();
        s.timeoutAt = LocalDateTime.now().plus(STEP_TIMEOUT);
        return s;
    }

    public void markStockReserved() {
        ensure(SagaStatus.STARTED);
        this.status = SagaStatus.STOCK_RESERVED;
        advance();
    }

    public void markWalletDebited() {
        ensure(SagaStatus.STOCK_RESERVED);
        this.status = SagaStatus.WALLET_DEBITED;
        advance();
    }

    public void markConfirmed() {
        ensure(SagaStatus.WALLET_DEBITED);
        this.status = SagaStatus.CONFIRMED;
        advance();
    }

    public void startCompensation(String reason) {
        if (status == SagaStatus.CONFIRMED || status == SagaStatus.CANCELED) {
            throw new IllegalStateException("현재 상태에서 이전상태로 복구가 불가능합니다. [%s]".formatted(status));
        }
        this.status = SagaStatus.COMPENSATING;
        this.errorReason = reason;
        advance();
    }

    public void markCancel() {
        ensure(SagaStatus.COMPENSATING);
        this.status = SagaStatus.CANCELED;
        advance();
    }

    private void ensure(SagaStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("주문의 현태 상태가 올바르지 않습니다. [가능상태 : %s] [상태 : %s]".formatted(expected, this.status));
        }
    }

    private void advance() {
        this.currentStep++;
        this.timeoutAt = LocalDateTime.now().plus(STEP_TIMEOUT);
    }
}
