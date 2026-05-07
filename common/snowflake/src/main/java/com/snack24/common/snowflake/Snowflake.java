package com.snack24.common.snowflake;

import java.security.SecureRandom;

/**
 * Twitter Snowflake 64-bit ID 생성기.
 * <p>
 * 구조: [unused 1bit | epoch 41bit | nodeId 10bit | sequence 12bit]
 * <ul>
 *   <li>같은 ms 안 4096개까지 발급 후 자동 대기</li>
 *   <li>nodeId는 {@link SecureRandom} 으로 인스턴스마다 무작위 지정 — 단일 JVM 다중 인스턴스에서 충돌 가능성 매우 낮음</li>
 *   <li>운영 환경에서 다수 노드 운영 시 nodeId 결정 전략(env, k8s pod ordinal 등)을 분리 권장</li>
 * </ul>
 */
public class Snowflake {

    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_NODE_ID = (1L << NODE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    // 2025-01-01T00:00:00Z (UTC) — 41bit epoch 시작점
    private static final long EPOCH_MILLIS = 1_735_689_600_000L;

    private final long nodeId = new SecureRandom().nextLong(MAX_NODE_ID + 1);

    private long lastTimeMillis = EPOCH_MILLIS;
    private long sequence = 0L;

    public synchronized long nextId() {
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis < lastTimeMillis) {
            // NTP 보정 등으로 시간이 거꾸로 갈 때 — 실서비스에선 모니터링/알림 대상
            throw new IllegalStateException("clock moved backwards");
        }

        if (currentTimeMillis == lastTimeMillis) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimeMillis = waitNextMillis(currentTimeMillis);
            }
        } else {
            sequence = 0L;
        }

        lastTimeMillis = currentTimeMillis;

        return ((currentTimeMillis - EPOCH_MILLIS) << (NODE_ID_BITS + SEQUENCE_BITS))
                | (nodeId << SEQUENCE_BITS)
                | sequence;
    }

    private long waitNextMillis(long currentTimeMillis) {
        while (currentTimeMillis <= lastTimeMillis) {
            currentTimeMillis = System.currentTimeMillis();
        }
        return currentTimeMillis;
    }
}
