package com.snack24.common.outboxrelay.outbox.event;

import com.snack24.common.outboxrelay.outbox.entity.Outbox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class OutboxEvent {
    private final Outbox outbox;
}
