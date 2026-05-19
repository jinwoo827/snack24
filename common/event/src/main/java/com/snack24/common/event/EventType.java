package com.snack24.common.event;

import com.snack24.common.event.payload.CompanyRegisterPayload;
import com.snack24.common.event.payload.MemberRegisteredPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {

    MEMBER_REGISTERED(MemberRegisteredPayload.class, Topic.IDENTITY),
    COMPANY_REGISTERED(CompanyRegisterPayload.class, Topic.IDENTITY),

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
