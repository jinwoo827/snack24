package com.snack24.billing.domain;

import com.snack24.billing.exception.BillingErrorCode;
import com.snack24.billing.exception.BillingException;
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
@ToString
@Table(name = "wallets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseEntity {

    @Id
    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    public static Wallet openFor(Long walletId, Long companyId) {
        Wallet w = new Wallet();
        w.walletId = walletId;
        w.companyId = companyId;
        w.balance = BigDecimal.ZERO;
        return w;
    }

    public void credit(BigDecimal amount) {
        requirePositive(amount);
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new BillingException(BillingErrorCode.INSUFFICIENT_BALANCE);
        }
        this.balance = this.balance.subtract(amount);
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("금액은 0이상이여야합니다");
        }
    }
}
