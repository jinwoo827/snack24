package com.snack24.order.web;

public record CallerContext(
        Long companyId,
        Long memberId,
        String role
) {
}
