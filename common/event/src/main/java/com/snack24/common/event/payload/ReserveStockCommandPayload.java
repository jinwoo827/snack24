package com.snack24.common.event.payload;

import com.snack24.common.event.EventPayload;
import com.snack24.common.event.SagaPayload;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReserveStockCommandPayload implements SagaPayload {
    private Long sagaId;
    private Long orderId;
    private Long companyId;
    private List<Item> items;

    @Getter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long productId;
        private int quantity;
    }

}
