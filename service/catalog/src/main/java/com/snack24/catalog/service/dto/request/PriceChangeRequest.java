package com.snack24.catalog.service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PriceChangeRequest(
        @NotNull @DecimalMin("0.0") BigDecimal unitPrice
        ) {
}
