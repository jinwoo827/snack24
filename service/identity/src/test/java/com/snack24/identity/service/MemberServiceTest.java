package com.snack24.identity.service;

import com.snack24.common.snowflake.Snowflake;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.exception.IdentityErrorCode;
import com.snack24.identity.exception.IdentityException;
import com.snack24.identity.repository.CompanyRepository;
import com.snack24.identity.repository.DepartmentRepository;
import com.snack24.identity.repository.MemberRepository;
import com.snack24.identity.service.dto.request.MemberRegisterRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock MemberRepository memberRepository;
    @Mock CompanyRepository companyRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock Snowflake snowflake;

    @InjectMocks MemberService memberService;

    @Test
    void emailIfDuplicated() {
        given(memberRepository.existsByEmail("test@test.co.kr")).willReturn(true);
        MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest(1L, null, "test@test.co.kr", "password", "홍길동", "01011112222", MemberRole.ROLE_MEMBER);
        Assertions.assertThatThrownBy(() -> memberService.register(memberRegisterRequest))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(IdentityErrorCode.EMAIL_DUPLICATED);
    }

    @Test
    void companyTest() {
        given(companyRepository.findById(any())).willReturn(Optional.empty());
        MemberRegisterRequest memberRegisterRequest = new MemberRegisterRequest(1L, null, "test@test.co.kr", "password", "홍길동", "01011112222", MemberRole.ROLE_MEMBER);
        Assertions.assertThatThrownBy(() -> memberService.register(memberRegisterRequest))
                .isInstanceOf(IdentityException.class)
                .extracting("errorCode").isEqualTo(IdentityErrorCode.COMPANY_NOT_FOUND);
    }
    

}