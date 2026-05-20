package com.snack24.catalog.service;

import com.snack24.catalog.domain.Product;
import com.snack24.catalog.domain.Stock;
import com.snack24.catalog.exception.CatalogErrorCode;
import com.snack24.catalog.exception.CatalogException;
import com.snack24.catalog.repository.ProductRepository;
import com.snack24.catalog.repository.StockRepository;
import com.snack24.catalog.service.dto.request.ProductCreateRequest;
import com.snack24.catalog.service.dto.response.ProductResponse;
import com.snack24.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final Snowflake snowflake;

    @Transactional
    public ProductResponse register(Long companyId, ProductCreateRequest request) {
        // product save
        Product product = Product.create(
                snowflake.nextId(),
                companyId,
                request.name(),
                request.description(),
                request.category(),
                request.unitPrice()
        );
        productRepository.save(product);

        // stock 빈 재고 save
        Stock emptyStock = Stock.createEmpty(
                snowflake.nextId(),
                product.getProductId(),
                companyId
        );
        stockRepository.save(emptyStock);
        return ProductResponse.from(product, emptyStock);
    }

    @Transactional
    public ProductResponse changePrice(Long productId, Long companyId, BigDecimal newPrice) {
        Product product = findOwned(productId, companyId);
        Stock stock = stockRepository.findByProductId(productId)
                        .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));
        product.changePrice(newPrice);
        return ProductResponse.from(product, stock);
    }

    public ProductResponse get(Long productId, Long companyId) {
        Product product = findOwned(productId, companyId);
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product, stock);
    }

    private Product findOwned(Long productId, Long companyId) {
        return productRepository.findById(productId)
                .filter(product -> product.getCompanyId().equals(companyId))
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.PRODUCT_NOT_FOUND));
    }
}
