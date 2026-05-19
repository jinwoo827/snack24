package com.snack24.catalog.repository;

import com.snack24.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
                                            ProductSearchRepository {
}
