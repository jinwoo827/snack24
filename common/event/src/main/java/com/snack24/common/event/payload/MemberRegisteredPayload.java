package com.snack24.common.event.payload;

import com.snack24.common.event.EventPayload;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 신규 직원 등록 완료 이벤트.
 * <p>구독자 예: notification-worker(환영 메일), billing(잔액 0으로 초기화는 직접 호출 — 이건 이벤트 X).
 */
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegisteredPayload implements EventPayload {
    private Long memberId;
    private Long companyId;
    private Long departmentId;
    private String email;
    private String name;
    private LocalDateTime registeredAt;
}
