package com.snack24.identity.auth;

import com.snack24.identity.domain.MemberRole;

import java.time.Instant;

public record AccessTokenClaims(
        Long memberId,
        Long companyId,
        MemberRole role,
        Instant expiresAt
) {
}
