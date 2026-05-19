package com.snack24.common.event.payload;

import com.snack24.common.event.EventPayload;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegisterPayload implements EventPayload {
    private Long companyId;
    private String name;
    private String businessNo;
    private String plan;
    private LocalDateTime joinedAt;
    private LocalDateTime registeredAt;
}
