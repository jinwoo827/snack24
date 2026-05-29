package com.snack24.billing.service;

import com.snack24.billing.domain.ProcessedMessage;
import com.snack24.billing.repository.ProcessedMessageRepository;
import com.snack24.common.event.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProcessedMessageService {
    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean markIfFirst(Long sagaId, EventType eventType) {
        if (processedMessageRepository.existsBySagaIdAndEventType(sagaId, eventType.name())) {
            return false;
        }
        try {
            processedMessageRepository.save(
                    ProcessedMessage.of(sagaId, eventType.name())
            );
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
