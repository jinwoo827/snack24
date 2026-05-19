package com.snack24.notification.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ProcessedEventRepository {
    private static final String KEY_PREFIX = "notification:processed:";
    private static final Duration TTL = Duration.ofDays(3);

    private final StringRedisTemplate redisTemplate;

    public boolean markIfFirst(Long eventId) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + eventId, "1", TTL);
        return Boolean.TRUE.equals(result);
    }
}
