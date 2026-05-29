package com.snack24.common.event.payload;

import com.snack24.common.event.EventPayload;
import com.snack24.common.event.SagaPayload;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DebitWalletCommandPayload implements SagaPayload {
    private Long sagaId;
    private Long orderId;
    private Long companyId;
    private BigDecimal amount;
}
