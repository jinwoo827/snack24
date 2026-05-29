package com.snack24.common.event.payload;

import com.snack24.common.event.EventPayload;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderCanceledPayload implements EventPayload {
    private Long orderId;
    private Long companyId;
    private String reason;
    private LocalDateTime canceledAt;
}
