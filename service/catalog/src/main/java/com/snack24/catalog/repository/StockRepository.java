package com.snack24.catalog.repository;

import com.snack24.catalog.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductId(Long productId);

    List<Stock> findByProductIdAndCompanyId(Long productId, Long companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.productId in :productIds")
    List<Stock> findAllByProductIdInForUpdate(@Param("productIds") List<Long> productIds);

    findStocksByOr
}
