package com.snack24.order.client.dto;

import java.math.BigDecimal;

public record ProductPrice(
        Long productId,
        String name,
        BigDecimal unitPrice
) {
}
