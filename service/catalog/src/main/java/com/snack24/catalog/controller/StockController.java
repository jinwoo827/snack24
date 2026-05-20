package com.snack24.catalog.controller;

import com.snack24.catalog.service.StockService;
import com.snack24.catalog.service.dto.request.StockReplenishRequest;
import com.snack24.catalog.service.dto.response.StockResponse;
import com.snack24.catalog.web.Caller;
import com.snack24.catalog.web.CallerContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/products/{productId}/stock")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping
    public StockResponse get(@Caller CallerContext caller, @PathVariable Long productId) {
        return stockService.get(productId, caller.companyId());
    }

    @PostMapping("/replenish")
    public ResponseEntity<Void> replenish(
            @Caller CallerContext caller,
            @PathVariable Long productId,
            @RequestBody @Valid StockReplenishRequest request
            ) {
        stockService.replenish(productId, caller.companyId(), request.quantity());
        return ResponseEntity.noContent().build();
    }



}
