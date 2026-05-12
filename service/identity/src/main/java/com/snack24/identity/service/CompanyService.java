package com.snack24.identity.service;

import com.snack24.common.snowflake.Snowflake;
import com.snack24.identity.domain.Company;
import com.snack24.identity.domain.CompanyPlan;
import com.snack24.identity.exception.IdentityErrorCode;
import com.snack24.identity.exception.IdentityException;
import com.snack24.identity.repository.CompanyRepository;
import com.snack24.identity.service.dto.request.CompanyRegisterRequest;
import com.snack24.identity.service.dto.response.CompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final Snowflake snowflake;

    @Transactional
    public CompanyResponse register(CompanyRegisterRequest request) {

        // 사업자 번호 중복 체크
        if (request.businessNo() != null && companyRepository.findCompanyByBusinessNo(request.businessNo()).isPresent()) {
            throw new IdentityException(IdentityErrorCode.BUSINESS_NO_DUPLICATED);
        }

        Company company = Company.register(
                snowflake.nextId(),
                request.name(),
                request.businessNo(),
                CompanyPlan.BASIC,
                LocalDateTime.now()
        );
        companyRepository.save(company);
        return CompanyResponse.from(company);
    }

    public CompanyResponse get(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.COMPANY_NOT_FOUND));
        return CompanyResponse.from(company);
    }
}
