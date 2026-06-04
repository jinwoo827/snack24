package com.snack24.notification.handler;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.OrderConfirmedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderConfirmedEventHandler implements EventHandler<OrderConfirmedPayload> {
    @Override
    public void handle(Event<OrderConfirmedPayload> event) {
        OrderConfirmedPayload payload = event.getPayload();
        log.info("[주문완료] orderId = {}, companyId = {}, memberId = {}, totalAmount = {} | 결제가 완료되었습니다."
                , payload.getOrderId(), payload.getCompanyId(), payload.getMemberId(), payload.getTotalAmount()
        );
    }

    @Override
    public boolean supports(Event<OrderConfirmedPayload> event) {
        return EventType.ORDER_CONFIRMED == event.getType();
    }
}
