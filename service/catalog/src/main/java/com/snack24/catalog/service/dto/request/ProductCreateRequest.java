package com.snack24.catalog.service.dto.request;

import com.snack24.catalog.domain.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull ProductCategory category,
        @NotNull @DecimalMin("0.0")BigDecimal unitPrice
        ) {
}
