package com.snack24.common.event;

import com.snack24.common.event.payload.MemberRegisteredPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도메인 이벤트의 단일 카탈로그.
 * <ul>
 *   <li>payload 클래스와 Kafka 토픽을 한 곳에서 묶어 관리 — 서비스 간 호환 깨짐 방지</li>
 *   <li>새 이벤트 추가 시 반드시 여기에 등록하고 payload 클래스 추가</li>
 * </ul>
 *
 * <p>Saga 흐름은 별도의 명령(Command) 이벤트로 다룬다 — 도메인 사실(Fact) 이벤트와 의도(Intent) 명령을
 * 섞지 않기 위함. 명령은 추후 별도 enum으로 분리 예정.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {

    // identity 도메인
    MEMBER_REGISTERED(MemberRegisteredPayload.class, Topic.IDENTITY),

    // catalog 도메인 — Week 2에 추가
    // PRODUCT_CREATED(...),

    // order / billing — Week 2~3에 추가
    ;

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] unknown type={}", type, e);
            return null;
        }
    }

    public static final class Topic {
        public static final String IDENTITY = "snack24-identity";
        public static final String CATALOG  = "snack24-catalog";
        public static final String ORDER    = "snack24-order";
        public static final String BILLING  = "snack24-billing";

        private Topic() {}
    }
}
