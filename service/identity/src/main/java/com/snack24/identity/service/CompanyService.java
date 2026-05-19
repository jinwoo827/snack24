package com.snack24.identity.service;

import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.CompanyRegisterPayload;
import com.snack24.common.event.payload.MemberRegisteredPayload;
import com.snack24.common.outboxrelay.outbox.event.OutboxEventPublisher;
import com.snack24.common.snowflake.Snowflake;
import com.snack24.identity.domain.Company;
import com.snack24.identity.domain.CompanyPlan;
import com.snack24.identity.domain.Member;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.exception.IdentityErrorCode;
import com.snack24.identity.exception.IdentityException;
import com.snack24.identity.repository.CompanyRepository;
import com.snack24.identity.repository.MemberRepository;
import com.snack24.identity.service.dto.request.CompanyRegisterAdminRequest;
import com.snack24.identity.service.dto.request.CompanyRegisterRequest;
import com.snack24.identity.service.dto.response.CompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Snowflake snowflake;
    private final OutboxEventPublisher outboxEventPublisher;

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

        String email = null;
        CompanyRegisterAdminRequest companyRegisterAdminRequest = request.admin();

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(companyRegisterAdminRequest.email())) {
            throw new IdentityException(IdentityErrorCode.EMAIL_DUPLICATED);
        }

        String hashedPassword = passwordEncoder.encode(companyRegisterAdminRequest.password());

        // member 저장
        Member member = Member.register(
                snowflake.nextId(),
                company.getCompanyId(),
                null,
                companyRegisterAdminRequest.email(),
                hashedPassword,
                companyRegisterAdminRequest.name(),
                companyRegisterAdminRequest.phone(),
                MemberRole.ROLE_COMPANY_ADMIN,
                LocalDateTime.now()
        );
        memberRepository.save(member);
        email = member.getEmail();

        outboxEventPublisher.publish(
                EventType.COMPANY_REGISTERED,
                CompanyRegisterPayload.builder()
                        .companyId(company.getCompanyId())
                        .name(company.getName())
                        .businessNo(company.getBusinessNo())
                        .plan(company.getPlan().name())
                        .joinedAt(company.getJoinedAt())
                        .registeredAt(company.getCreatedAt())
                        .build(),
                company.getCompanyId()
                );

        outboxEventPublisher.publish(
                EventType.MEMBER_REGISTERED,
                MemberRegisteredPayload.builder()
                        .memberId(member.getMemberId())
                        .companyId(member.getCompanyId())
                        .departmentId(null)
                        .email(member.getEmail())
                        .name(member.getName())
                        .registeredAt(member.getCreatedAt())
                        .build(),
                member.getMemberId()
        );

        return CompanyResponse.from(company, email);
    }

    public CompanyResponse get(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.COMPANY_NOT_FOUND));
        return CompanyResponse.from(company);
    }
}
