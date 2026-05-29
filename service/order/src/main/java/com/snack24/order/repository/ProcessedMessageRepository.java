package com.snack24.order.repository;

import com.snack24.common.event.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ProcessedMessageRepository {
    private static final String KEY_PREFIX = "saga:processed:%s:%s";
    private static final Duration TTL = Duration.ofDays(3);

    private final StringRedisTemplate redisTemplate;

    public boolean markIfFirst(Long sagaId, EventType eventType) {
        String key = KEY_PREFIX.formatted(sagaId, eventType.name());

        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", TTL);
        return Boolean.TRUE.equals(result);
    }
}
