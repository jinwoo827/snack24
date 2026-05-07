package com.snack24.common.event;

/**
 * 모든 도메인 이벤트 payload의 마커 인터페이스.
 * Jackson 다형성 역직렬화 시 {@link Event}의 generic 경계를 좁히는 용도.
 */
public interface EventPayload {
}
