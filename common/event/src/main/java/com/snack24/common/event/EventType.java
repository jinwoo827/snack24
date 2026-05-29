package com.snack24.common.event;

import com.snack24.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {

    // identity
    MEMBER_REGISTERED(MemberRegisteredPayload.class, Topic.IDENTITY),
    COMPANY_REGISTERED(CompanyRegisterPayload.class, Topic.IDENTITY),

    // Saga 명령 (order -> catalog / billing)
    RESERVE_STOCK_COMMAND(ReserveStockCommandPayload.class, Topic.CATALOG),
    RELEASE_STOCK_COMMAND(ReleaseStockCommandPayload.class, Topic.CATALOG),
    DEBIT_WALLET_COMMAND(DebitWalletCommandPayload.class, Topic.BILLING),

    // Saga 응답 이벤트 (catalog / billing -> order)
    STOCK_RESERVED(StockReservedPayload.class, Topic.CATALOG),
    STOCK_RESERVATION_FAILED(StockReservationFailedPayload.class, Topic.CATALOG),
    STOCK_RELEASED(StockReleasedPayload.class, Topic.CATALOG),
    WALLET_DEBITED(WalletDebitedPayload.class, Topic.BILLING),
    WALLET_DEBIT_FAILED(WalletDebitFailedPayload.class, Topic.BILLING),

    // order -> notification
    ORDER_CONFIRMED(OrderConfirmedPayload.class, Topic.ORDER),
    ORDER_CANCELED(OrderCanceledPayload.class, Topic.ORDER),

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
