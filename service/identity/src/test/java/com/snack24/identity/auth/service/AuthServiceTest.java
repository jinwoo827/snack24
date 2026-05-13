package com.snack24.identity.auth.service;

import com.snack24.identity.auth.repository.RefreshTokenRepository;
import com.snack24.identity.auth.service.dto.request.LoginRequest;
import com.snack24.identity.auth.service.dto.request.RefreshRequest;
import com.snack24.identity.auth.service.dto.response.LoginResponse;
import com.snack24.identity.domain.Member;
import com.snack24.identity.domain.MemberRole;
import com.snack24.identity.exception.IdentityException;
import com.snack24.identity.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class AuthServiceTest {

    @Autowired AuthService authService;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired RefreshTokenRepository refreshTokenRepository;

    @Test
    void authServiceTest() {
        // given
        String email = "jinwoo1@test.co.kr";
        String password = "jinwoopwd";
        String hashedPassword = passwordEncoder.encode(password);
        //Member member = memberRepository.save(Member.register(100L, 1L, null, email, hashedPassword, "이진우", "01072729323", MemberRole.ROLE_MEMBER, LocalDateTime.now()));
        LoginResponse loginResponse = authService.login(new LoginRequest(email, password));

        log.info("loginResponse = {}", loginResponse);

        // when : refresh
        LoginResponse refreshResponse = authService.refresh(new RefreshRequest(loginResponse.refreshToken()));
        log.info("refreshResponse = {}", refreshResponse);

        Assertions.assertThat(loginResponse.accessToken()).isNotEqualTo(refreshResponse.accessToken());
        Assertions.assertThat(loginResponse.refreshToken()).isNotEqualTo(refreshResponse.refreshToken());

        // 지난 토큰 재사용시 -> 토큰 모두 삭제
        Assertions.assertThatThrownBy(() -> authService.refresh(new RefreshRequest(loginResponse.refreshToken())))
                .isInstanceOf(IdentityException.class);

        // 토큰 삭제되었는지 체크
        Assertions.assertThat(refreshTokenRepository.find(refreshResponse.refreshToken())).isEmpty();
    }

}