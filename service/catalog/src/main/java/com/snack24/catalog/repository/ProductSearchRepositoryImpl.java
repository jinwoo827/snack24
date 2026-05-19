package com.snack24.catalog.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snack24.catalog.domain.ProductCategory;
import com.snack24.catalog.domain.ProductStatus;
import com.snack24.catalog.domain.QProduct;
import com.snack24.catalog.domain.QStock;
import com.snack24.catalog.repository.dto.ProductListItem;
import com.snack24.catalog.repository.dto.ProductSearchCondition;
import com.snack24.catalog.repository.dto.QProductListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.snack24.catalog.domain.QProduct.*;
import static com.snack24.catalog.domain.QStock.*;

@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final JPAQueryFactory query;

    @Override
    public Page<ProductListItem> searchAdmin(ProductSearchCondition cond, Pageable pageable) {

        // contents
        List<ProductListItem> contents = query.select(new QProductListItem(
                        product.productId,
                        product.name,
                        product.category,
                        product.unitPrice,
                        product.status,
                        stock.totalQty,
                        stock.lockedQty
                ))
                .from(product)
                .innerJoin(stock)
                .on(product.companyId.eq(stock.companyId)
                        .and(product.productId.eq(stock.productId)))
                .where(
                        companyIdEq(cond.getCompanyId()),
                        categoryEq(cond.getCategory()),
                        statusEq(cond.getStatus()),
                        nameLike(cond.getName()),
                        goePrice(cond.getMinPrice()),
                        loePrice(cond.getMaxPrice())
                )
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count
        JPAQuery<Long> countQuery = query
                .select(product.count())
                .from(product)
                .where(
                        companyIdEq(cond.getCompanyId()),
                        categoryEq(cond.getCategory()),
                        statusEq(cond.getStatus()),
                        nameLike(cond.getName()),
                        goePrice(cond.getMinPrice()),
                        loePrice(cond.getMaxPrice())
                );

        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    private BooleanExpression companyIdEq(Long companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
        return product.companyId.eq(companyId);
    }

    private BooleanExpression categoryEq(ProductCategory category) {
        return category != null ? product.category.eq(category) : null;
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status != null ? product.status.eq(status) : null;
    }

    private BooleanExpression nameLike(String name) {
        return StringUtils.hasText(name) ? product.name.contains(name) : null;
    }

    private BooleanExpression goePrice(BigDecimal minPrice) {
        return minPrice != null ? product.unitPrice.goe(minPrice) : null;
    }

    private BooleanExpression loePrice(BigDecimal maxPrice) {
        return maxPrice != null ? product.unitPrice.loe(maxPrice) : null;
    }

}
