package com.snack24.identity.auth;

import com.snack24.identity.domain.MemberRole;

public record MemberPrincipal(
        Long memberId,
        Long companyId,
        MemberRole role
) {
    public String roleName() {
        return role.name();
    }
}
