package com.snack24.catalog.repository;

import com.snack24.catalog.domain.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, ProcessedMessage.ProcessedMessageId> {
    boolean existsBySagaIdAndEventType(Long sagaId, String eventType);
}
