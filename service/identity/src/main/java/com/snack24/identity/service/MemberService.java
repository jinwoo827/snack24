package com.snack24.identity.service;

import com.snack24.common.jpabase.exception.ErrorCode;
import com.snack24.common.snowflake.Snowflake;
import com.snack24.identity.domain.Company;
import com.snack24.identity.domain.Department;
import com.snack24.identity.domain.Member;
import com.snack24.identity.exception.IdentityErrorCode;
import com.snack24.identity.exception.IdentityException;
import com.snack24.identity.repository.CompanyRepository;
import com.snack24.identity.repository.DepartmentRepository;
import com.snack24.identity.repository.MemberRepository;
import com.snack24.identity.service.dto.request.MemberRegisterRequest;
import com.snack24.identity.service.dto.response.MemberResponse;
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
public class MemberService {
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final Snowflake snowflake;

    @Transactional
    public MemberResponse register(MemberRegisterRequest request) {

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(request.email())) {
            throw new IdentityException(IdentityErrorCode.EMAIL_DUPLICATED);
        }

        // 회사 검증
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.COMPANY_NOT_FOUND));

        // 부서 검증
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IdentityException(IdentityErrorCode.DEPARTMENT_NOT_FOUND));
            if (!department.getCompanyId().equals(company.getCompanyId())) {
                throw new IdentityException(IdentityErrorCode.DEPARTMENT_COMPANY_MISMATCH);
            }
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        // member 저장
        Member member = Member.register(
                snowflake.nextId(),
                request.companyId(),
                request.departmentId(),
                request.email(),
                hashedPassword,
                request.name(),
                request.phone(),
                request.role(),
                LocalDateTime.now()
        );
        memberRepository.save(member);
        return MemberResponse.from(member);
    }

    public MemberResponse get(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.MEMBER_NOT_FOUND));
        return MemberResponse.from(member);
    }
}
