package com.snack24.catalog.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.snack24.catalog.domain.ProductCategory;
import com.snack24.catalog.domain.ProductStatus;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
public class ProductListItem {
    private final Long productId;
    private final String name;
    private final ProductCategory category;
    private final BigDecimal unitPrice;
    private final ProductStatus status;
    private final int totalQty;
    private final int lockedQty;
    private final int availableQty;

    @QueryProjection
    public ProductListItem(Long productId, String name, ProductCategory category, BigDecimal unitPrice, ProductStatus status, int totalQty, int lockedQty) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.status = status;
        this.totalQty = totalQty;
        this.lockedQty = lockedQty;
        this.availableQty = totalQty - lockedQty;
    }
}
