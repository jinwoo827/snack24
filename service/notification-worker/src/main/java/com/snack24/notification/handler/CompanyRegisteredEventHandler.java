package com.snack24.notification.handler;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.CompanyRegisterPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CompanyRegisteredEventHandler implements EventHandler<CompanyRegisterPayload> {
    @Override
    public void handle(Event<CompanyRegisterPayload> event) {
        CompanyRegisterPayload payload = event.getPayload();
        log.info("[임의 알림 발송] {} (사업자번호 {})으로 등록 되었습니다.", payload.getName(), payload.getBusinessNo());
    }

    @Override
    public boolean supports(Event<CompanyRegisterPayload> event) {
        return EventType.COMPANY_REGISTERED == event.getType();
    }
}
