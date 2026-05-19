package com.snack24.catalog.domain;

import com.snack24.catalog.exception.CatalogErrorCode;
import com.snack24.catalog.exception.CatalogException;
import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@Table(name = "stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {
    @Id
    @Column(name = "stock_id")
    private Long stockId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "total_qty", nullable = false)
    private int totalQty;

    @Column(name = "locked_qty", nullable = false)
    private int lockedQty;

    public int getAvailableQty() {
        return totalQty - lockedQty;
    }

    public static Stock createEmpty(Long stockId, Long productId, Long companyId) {
        Stock s = new Stock();
        s.stockId = stockId;
        s.productId = productId;
        s.companyId = companyId;
        s.totalQty = 0;
        s.lockedQty = 0;
        return s;
    }

    // -- 입고 --
    public void replenish(int qty) {
        requirePositive(qty);
        this.totalQty += qty;
    }

    // Saga 1 단계 : 재고 예약
    public void reserve(int qty) {
        requirePositive(qty);
        if (getAvailableQty() < qty) {
            throw new CatalogException(CatalogErrorCode.INSUFFICIENT_STOCK);
        }
        this.lockedQty += qty;
    }

    // Saga 보상 : 예약 해제
    public void release(int qty) {
        requirePositive(qty);
        if (this.lockedQty < qty) {
            throw new IllegalStateException("예약된 재고수량을 초과합니다.");
        }
        this.lockedQty -= qty;
    }

    // Saga 확정 : 예약을 실제 재고 차감
    public void confirm(int qty) {
        requirePositive(qty);
        if (this.lockedQty < qty) {
            throw new IllegalStateException("예약된 재고수량을 초과합니다.");
        }
        this.lockedQty -= qty;
        this.totalQty -= qty;
    }

    private static void requirePositive(int qty) {
        if (qty <= 0) {
            throw new IllegalStateException("수량은 양수이어야합니다. " + qty);
        }
    }
}
