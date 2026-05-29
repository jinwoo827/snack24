package com.snack24.order.service.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OrderPlaceRequest(
        @NotEmpty(message = "주문 품목은 필수입니다.")
        @Size(max = 50, message = "한 주문에 최대 50 품목만 가능합니다.")
        @Valid
        List<Item> items
) {

        public record Item(
                @NotNull Long productId,
                @Min(value = 1, message = "수량은 최소 1이상이여야 합니다.")
                @Max(value = 999, message = "수량은 최대 999개까지 가능합니다.")
                Integer quantity
        ) {
        }
}
