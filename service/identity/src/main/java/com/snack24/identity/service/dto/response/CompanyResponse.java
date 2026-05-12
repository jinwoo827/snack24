package com.snack24.identity.service.dto.response;

import com.snack24.identity.domain.Company;
import com.snack24.identity.domain.CompanyPlan;
import com.snack24.identity.domain.CompanyStatus;

import java.time.LocalDateTime;

public record CompanyResponse(
        Long companyId,
        String name,
        String businessNo,
        CompanyPlan plan,
        CompanyStatus status,
        LocalDateTime joinedAt,
        LocalDateTime createdAt
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getCompanyId(),
                company.getName(),
                company.getBusinessNo(),
                company.getPlan(),
                company.getStatus(),
                company.getJoinedAt(),
                company.getCreatedAt()
        );
    }
}
