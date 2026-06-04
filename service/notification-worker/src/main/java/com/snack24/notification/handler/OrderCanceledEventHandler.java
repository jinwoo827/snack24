package com.snack24.notification.handler;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.OrderCanceledPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCanceledEventHandler implements EventHandler<OrderCanceledPayload> {
    @Override
    public void handle(Event<OrderCanceledPayload> event) {
        OrderCanceledPayload payload = event.getPayload();
        log.info("[주문 취소] orderId = {}, companyId = {}, reason = {} | 주문이 취소되었습니다."
                , payload.getOrderId(), payload.getCompanyId(), payload.getReason()
        );
    }

    @Override
    public boolean supports(Event<OrderCanceledPayload> event) {
        return EventType.ORDER_CANCELED == event.getType();
    }
}
