package com.snack24.catalog.repository;

import com.snack24.catalog.repository.dto.ProductListItem;
import com.snack24.catalog.repository.dto.ProductSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchRepository {
    Page<ProductListItem> searchAdmin(ProductSearchCondition cond, Pageable pageable);
}
