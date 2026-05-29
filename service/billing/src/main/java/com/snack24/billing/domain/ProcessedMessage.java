package com.snack24.billing.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Entity
@Table(name = "processed_messages")
@IdClass(ProcessedMessage.ProcessedMessageId.class)
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedMessage {

    @Id
    @Column(name = "saga_id")
    private Long sagaId;

    @Id
    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "processed_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime processedAt;

    public static ProcessedMessage of(Long sagaId, String eventType) {
        ProcessedMessage pm = new ProcessedMessage();
        pm.sagaId = sagaId;
        pm.eventType = eventType;
        pm.processedAt = LocalDateTime.now();
        return pm;
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class ProcessedMessageId {
        private Long sagaId;
        private String eventType;
    }
}
