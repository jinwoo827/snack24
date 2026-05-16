package com.snack24.identity.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRegisterRequest(
        @NotBlank @Size(max = 100) String name,

        @Pattern(
                regexp = "^(\\d{3}-\\d{2}-\\d{5}|\\d{10})$",
                message = "사업자번호 형식이 올바르지 않습니다."
        )
        @Size(max = 12) String businessNo,
        CompanyRegisterAdminRequest admin
) {
        public String normalizedBusinessNo() {
                return businessNo == null ? null : businessNo.replace("-", "");
        }
}
