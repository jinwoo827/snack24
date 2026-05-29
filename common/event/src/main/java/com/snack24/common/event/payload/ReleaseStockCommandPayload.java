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
public class ReleaseStockCommandPayload implements SagaPayload {
    private Long sagaId;
    private Long orderId;
    private List<ReserveStockCommandPayload.Item> items;
}
