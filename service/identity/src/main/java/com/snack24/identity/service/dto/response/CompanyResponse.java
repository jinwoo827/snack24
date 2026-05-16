package com.snack24.identity.service.dto.response;

import com.snack24.identity.domain.Company;
import com.snack24.identity.domain.CompanyPlan;
import com.snack24.identity.domain.CompanyStatus;
import com.snack24.identity.domain.Member;
import lombok.With;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long companyId,
        String name,
        String businessNo,
        CompanyPlan plan,
        CompanyStatus status,
        LocalDateTime joinedAt,
        LocalDateTime createdAt,
        @With String email
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getCompanyId(),
                company.getName(),
                company.getBusinessNo(),
                company.getPlan(),
                company.getStatus(),
                company.getJoinedAt(),
                company.getCreatedAt(),
                null
        );
    }

    public static CompanyResponse from(Company company, String email) {
        CompanyResponse response = from(company);
        return response.withEmail(email);
    }

}
