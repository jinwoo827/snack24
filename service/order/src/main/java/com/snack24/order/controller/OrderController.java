package com.snack24.order.controller;

import com.snack24.order.service.OrderService;
import com.snack24.order.service.request.OrderPlaceRequest;
import com.snack24.order.service.response.OrderResponse;
import com.snack24.order.web.Caller;
import com.snack24.order.web.CallerContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> place(
            @Caller CallerContext caller,
            @RequestBody @Valid OrderPlaceRequest request
    ) {
        Long orderId = orderService.place(
                caller.companyId(),
                caller.memberId(),
                caller.role(),
                request
        );
        URI location = URI.create("/v1/orders/" + orderId);
        return ResponseEntity.accepted()
                .location(location)
                .body(Map.of("orderId", orderId));
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(
            @Caller CallerContext caller,
            @PathVariable Long orderId
    ) {
        return orderService.get(orderId, caller.companyId());
    }
}
