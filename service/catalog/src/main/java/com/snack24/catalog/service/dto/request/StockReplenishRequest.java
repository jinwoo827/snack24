package com.snack24.catalog.service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockReplenishRequest(
        @NotNull @Min(1) Integer quantity
) {
}
