package com.snack24.gateway.jwt;

public record AccessTokenClaims(
        Long memberId,
        Long companyId,
        String role
) {
}
