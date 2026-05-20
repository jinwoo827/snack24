package com.snack24.catalog.repository.dto;

import com.snack24.catalog.domain.ProductCategory;
import com.snack24.catalog.domain.ProductStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductSearchCondition {
    @With private Long companyId;
    private ProductCategory category;
    private ProductStatus status;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
