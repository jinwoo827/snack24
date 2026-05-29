package com.snack24.common.event;

public interface SagaPayload extends EventPayload {
    Long getSagaId();
    Long getOrderId();
}
