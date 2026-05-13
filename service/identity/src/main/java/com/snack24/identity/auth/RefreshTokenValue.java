package com.snack24.identity.auth;

import java.time.Instant;

public record RefreshTokenValue(
        Long memberId,
        Instant expiredAt
) {
}
