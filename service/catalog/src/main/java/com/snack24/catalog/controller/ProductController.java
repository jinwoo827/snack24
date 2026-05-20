package com.snack24.catalog.controller;

import com.snack24.catalog.repository.ProductRepository;
import com.snack24.catalog.repository.dto.ProductListItem;
import com.snack24.catalog.repository.dto.ProductSearchCondition;
import com.snack24.catalog.service.ProductService;
import com.snack24.catalog.service.dto.request.PriceChangeRequest;
import com.snack24.catalog.service.dto.request.ProductCreateRequest;
import com.snack24.catalog.service.dto.response.ProductResponse;
import com.snack24.catalog.web.Caller;
import com.snack24.catalog.web.CallerContext;
import com.snack24.common.jpabase.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<ProductResponse> register(
            @Caller CallerContext caller,
            @RequestBody @Valid ProductCreateRequest request
    ) {
        ProductResponse response = productService.register(caller.companyId(), request);
        return ResponseEntity.created(URI.create("/v1/products/" + response.productId())).body(response);
    }

    @GetMapping("/{productId}")
    public ProductResponse get(@Caller CallerContext caller, @PathVariable Long productId) {
        return productService.get(productId, caller.companyId());
    }

    @PatchMapping("/{productId}/price")
    public ProductResponse changePrice(
            @Caller CallerContext caller,
            @PathVariable Long productId,
            @RequestBody @Valid PriceChangeRequest request) {
        return productService.changePrice(productId, caller.companyId(), request.unitPrice());
    }

    @GetMapping
    public PageResponse<ProductListItem> search(
            @Caller CallerContext caller,
            @ModelAttribute ProductSearchCondition cond,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
            ) {
        ProductSearchCondition safeCond = cond.withCompanyId(caller.companyId());
        return PageResponse.from(productRepository.searchAdmin(safeCond, pageable));
    }
}
