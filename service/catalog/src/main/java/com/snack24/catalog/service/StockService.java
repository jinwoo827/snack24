package com.snack24.catalog.service;

import com.snack24.catalog.domain.Stock;
import com.snack24.catalog.exception.CatalogErrorCode;
import com.snack24.catalog.exception.CatalogException;
import com.snack24.catalog.repository.StockRepository;
import com.snack24.catalog.service.dto.response.StockResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void replenish(Long productId, Long companyId, int qty) {
        Stock stock = getStock(productId, companyId);
        stock.replenish(qty);
    }

    public StockResponse get(Long productId, Long companyId) {
        Stock stock = getStock(productId, companyId);
        return StockResponse.from(stock);
    }

    private Stock getStock(Long productId, Long companyId) {
        return stockRepository.findByProductId(productId)
                .filter(s -> s.getCompanyId().equals(companyId))
                .orElseThrow(() -> new CatalogException(CatalogErrorCode.STOCK_NOT_FOUND));
    }
}
