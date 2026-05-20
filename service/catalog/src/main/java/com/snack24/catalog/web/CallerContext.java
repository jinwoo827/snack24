package com.snack24.catalog.web;

public record CallerContext(
        Long companyId,
        Long memberId,
        String role
) {
}
