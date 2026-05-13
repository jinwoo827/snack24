package com.snack24.identity.auth.service.dto.response;

import com.snack24.identity.domain.MemberRole;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long memberId,
        Long companyId,
        MemberRole role,
        long accessTokenExpiresInSeconds
) {
}
