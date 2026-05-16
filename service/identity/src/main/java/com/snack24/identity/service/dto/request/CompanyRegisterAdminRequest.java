package com.snack24.identity.service.dto.request;

public record CompanyRegisterAdminRequest(
        String email,
        String password,
        String name,
        String phone
) {
}
