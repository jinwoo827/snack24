package com.snack24.order.service;

import com.snack24.common.event.EventType;
import com.snack24.order.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProcessedMessageService {
    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional
    public boolean markIfFirst(Long sagaId, EventType eventType) {
        return processedMessageRepository.markIfFirst(sagaId, eventType);
    }
}
