package com.snack24.catalog.service.dto.response;

import com.snack24.catalog.domain.Product;
import com.snack24.catalog.domain.ProductCategory;
import com.snack24.catalog.domain.ProductStatus;
import com.snack24.catalog.domain.Stock;

import java.math.BigDecimal;

public record ProductResponse(
        Long productId,
        Long companyId,
        String name,
        String description,
        ProductCategory category,
        BigDecimal unitPrice,
        ProductStatus status,
        int totalQty,
        int lockedQty,
        int availableQty
) {
    public static ProductResponse from(Product product, Stock stock) {
        return new ProductResponse(
                product.getProductId(),
                product.getCompanyId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getUnitPrice(),
                product.getStatus(),
                stock.getTotalQty(),
                stock.getLockedQty(),
                stock.getAvailableQty()
        );
    }
}
