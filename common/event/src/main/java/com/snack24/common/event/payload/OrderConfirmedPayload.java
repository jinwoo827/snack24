package com.snack24.common.event.payload;

import com.snack24.common.event.EventPayload;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedPayload implements EventPayload {
    private Long orderId;
    private Long companyId;
    private Long memberId;
    private BigDecimal totalAmount;
    private LocalDateTime confirmedAt;
}
