package com.snack24.common.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.snack24.common.dataserializer.DataSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Kafka로 흘러다니는 모든 도메인 이벤트의 공통 봉투(envelope).
 * <pre>
 * { "eventId": ..., "type": "MEMBER_REGISTERED", "payload": { ... } }
 * </pre>
 *
 * <p>{@code payload}는 {@link EventType#getPayloadClass()}로 매핑되어 역직렬화된다.
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Event<T extends EventPayload> {

    private Long eventId;
    private EventType type;
    private T payload;

    public static <T extends EventPayload> Event<T> of(Long eventId, EventType type, T payload) {
        Event<T> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    public String toJson() {
        return DataSerializer.serialize(this);
    }

    /**
     * Kafka 메시지(JSON) → Event<TypedPayload> 로 복원.
     * type 필드를 먼저 읽어 payload 클래스를 결정한 뒤 strong-typed로 역직렬화.
     */
    @SuppressWarnings("unchecked")
    public static Event<EventPayload> fromJson(String json) {
        EventRaw raw = DataSerializer.deserialize(json, EventRaw.class);
        if (raw == null || raw.type == null) {
            return null;
        }
        EventPayload payload = (EventPayload) DataSerializer.deserialize(
                raw.payload.toString(),
                raw.type.getPayloadClass()
        );
        Event<EventPayload> event = new Event<>();
        event.eventId = raw.eventId;
        event.type = raw.type;
        event.payload = payload;
        return event;
    }

    @Getter
    @NoArgsConstructor
    private static class EventRaw {
        private Long eventId;
        private EventType type;
        private JsonNode payload;
    }
}
