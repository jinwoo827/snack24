package com.snack24.catalog.domain;

import com.snack24.common.jpabase.config.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Getter
@ToString
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ProductCategory category;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    public static Product create(Long productId, Long companyId, String name, String description, ProductCategory category, BigDecimal unitPrice) {
        Product p = new Product();
        p.productId = productId;
        p.companyId = companyId;
        p.name = name;
        p.description = description;
        p.category = category;
        p.status = ProductStatus.ON_SALE;
        p.unitPrice = unitPrice;
        return p;
    }

    public void changePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() < 0) {
            throw new IllegalArgumentException("단가는 0이상이여야 합니다.");
        }
        this.unitPrice = newPrice;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public void markSoldOut() {
        this.status = ProductStatus.SOLD_OUT;
    }

    public void markOnSale() {
        this.status = ProductStatus.ON_SALE;
    }
}
