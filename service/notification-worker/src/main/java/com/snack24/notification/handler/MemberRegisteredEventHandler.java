package com.snack24.notification.handler;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.MemberRegisteredPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberRegisteredEventHandler implements EventHandler<MemberRegisteredPayload> {
    @Override
    public void handle(Event<MemberRegisteredPayload> event) {
        MemberRegisteredPayload payload = event.getPayload();
        log.info("[임의 알림 발송] to = {} | {}님, 가입을 환영합니다.", payload.getEmail(), payload.getName());
    }

    @Override
    public boolean supports(Event<MemberRegisteredPayload> event) {
        return EventType.MEMBER_REGISTERED == event.getType();
    }
}
