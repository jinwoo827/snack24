package com.snack24.billing.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@ToString
@Getter
@Table(name = "wallet_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction extends BaseEntity {
    @Id
    @Column(name = "tx_id")
    private Long txId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TxType type;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // 항상 양수값

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 20)
    private RefType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "saga_id")
    private Long sagaId;

    @Column(name = "memo", length = 500)
    private String memo;

    // 충전
    public static WalletTransaction charge(Long txId, Long walletId, BigDecimal amount, String memo) {
        return create(txId, walletId, TxType.CHARGE, amount, RefType.MANUAL, null, null, memo);
    }

    // 출금
    public static WalletTransaction debit(Long txId, Long walletId, BigDecimal amount, Long orderId, Long sagaId) {
        return create(txId, walletId, TxType.DEBIT, amount, RefType.ORDER, orderId, sagaId, null);
    }

    // 환불
    public static WalletTransaction refund(Long txId, Long walletId, BigDecimal amount, Long orderId, Long sagaId) {
        return create(txId, walletId, TxType.REFUND, amount, RefType.ORDER, orderId, sagaId, "saga compensation");
    }

    public static WalletTransaction adjust(Long txId, Long walletId, BigDecimal amount, String memo) {
        return create(txId, walletId, TxType.ADJUST, amount, RefType.MANUAL, null, null, memo);
    }

    private static WalletTransaction create(Long txId, Long walletId, TxType type, BigDecimal amount, RefType refType, Long refId, Long sagaId, String memo) {
        WalletTransaction tx = new WalletTransaction();
        tx.txId = txId;
        tx.walletId = walletId;
        tx.type = type;
        tx.amount = amount;
        tx.referenceType = refType;
        tx.referenceId = refId;
        tx.sagaId = sagaId;
        tx.memo = memo;
        return tx;
    }
}
