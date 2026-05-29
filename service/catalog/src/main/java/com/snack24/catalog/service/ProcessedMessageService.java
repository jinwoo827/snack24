package com.snack24.catalog.service;

import com.snack24.catalog.domain.ProcessedMessage;
import com.snack24.catalog.repository.ProcessedMessageRepository;
import com.snack24.common.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProcessedMessageService {
    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean markIfFirst(Long sagaId, EventType eventType) {
        if (processedMessageRepository.existsBySagaIdAndEventType(sagaId, eventType.name())) {
            return false;
        }
        try {
            processedMessageRepository.save(ProcessedMessage.of(sagaId, eventType.name()));
            return true;
        } catch (DataIntegrityViolationException e) {
            log.error("[ProcessedMessageService.markIfFirst] duplicated sagaId = {}, eventType = {}", sagaId, eventType.name());
            return false;
        }
    }
}
