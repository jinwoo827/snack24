package com.snack24.billing.web;

public record CallerContext(
        Long companyId,
        Long memberId,
        String role
) {
}
