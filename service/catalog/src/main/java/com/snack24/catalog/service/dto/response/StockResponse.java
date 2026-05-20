package com.snack24.catalog.service.dto.response;

import com.snack24.catalog.domain.Stock;

public record StockResponse(
        Long productId,
        int totalQty,
        int lockedQty,
        int availableQty
) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getProductId(),
                stock.getTotalQty(),
                stock.getLockedQty(),
                stock.getAvailableQty()
        );
    }
}
