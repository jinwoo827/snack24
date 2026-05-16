package com.snack24.identity.service.dto.request;

import com.snack24.identity.domain.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.With;

public record MemberRegisterRequest(
        @With @NotNull Long companyId,
        Long departmentId,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 50) String name,
        @Size(max = 20) String phone,
        @NotNull MemberRole role
        ) {
}
