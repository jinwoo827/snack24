package com.snack24.notification.handler;

import com.snack24.common.event.Event;
import com.snack24.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
