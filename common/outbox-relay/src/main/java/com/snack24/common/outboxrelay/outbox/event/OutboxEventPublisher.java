package com.snack24.common.outboxrelay.outbox.event;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventPayload;
import com.snack24.common.event.EventType;
import com.snack24.common.outboxrelay.outbox.entity.Outbox;
import com.snack24.common.outboxrelay.relay.MessageRelayConstants;
import com.snack24.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final Snowflake outboxIdSnowFlake = new Snowflake();
    private final Snowflake eventIdSnowFlake = new Snowflake();
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(EventType eventType, EventPayload eventPayload, Long shardKey) {
        Outbox outbox = Outbox.create(
                outboxIdSnowFlake.nextId(),
                eventType,
                Event.of(eventIdSnowFlake.nextId(), eventType, eventPayload).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT
        );
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
