package com.snack24.common.event.payload;

import com.snack24.common.event.EventPayload;
import com.snack24.common.event.SagaPayload;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WalletDebitFailedPayload implements SagaPayload {
    private Long sagaId;
    private Long orderId;
    private String reason;
}
