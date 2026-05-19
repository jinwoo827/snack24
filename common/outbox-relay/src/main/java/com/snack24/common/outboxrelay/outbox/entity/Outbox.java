package com.snack24.common.outboxrelay.outbox.entity;

import com.snack24.common.event.EventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {
    @Id
    private Long outboxId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private EventType eventType;

    @Column(nullable = false, columnDefinition = "JSON")
    private String payload;

    @Column(nullable = false)
    private Long shardKey;

    @Column(nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    public static Outbox create(Long outboxId, EventType eventType, String payload, Long shardKey) {
        Outbox outbox = new Outbox();
        outbox.outboxId = outboxId;
        outbox.eventType = eventType;
        outbox.payload = payload;
        outbox.shardKey = shardKey;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }
}
