package com.snack24.billing.consumer;

import com.snack24.billing.service.WalletCommandHandler;
import com.snack24.common.event.Event;
import com.snack24.common.event.EventPayload;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.DebitWalletCommandPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingEventConsumer {
    private final WalletCommandHandler walletCommandHandler;

    @KafkaListener(topics = EventType.Topic.BILLING)
    public void listen(String message, Acknowledgment ack) {
        Event<EventPayload> event = Event.fromJson(message);
        if (event == null) {
            ack.acknowledge();
            return;
        }

        switch (event.getType()) {
            case EventType.DEBIT_WALLET_COMMAND -> walletCommandHandler.handleDebit((DebitWalletCommandPayload) event.getPayload());
            default -> {}
        }

        ack.acknowledge();
    }
}
