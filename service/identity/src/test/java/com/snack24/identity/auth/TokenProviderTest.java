package com.snack24.identity.auth;

import com.snack24.identity.domain.MemberRole;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TokenProviderTest {

    private TokenProvider provider;
    private static final String ISSUER = "snack24-identity";
    private static final String SECRET = "test-secret-32byte-min-test-secret";

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString(SECRET.getBytes());
        JwtProperties jwtProperties = new JwtProperties(
                ISSUER,
                secret,
                Duration.ofMinutes(30),
                Duration.ofDays(14)
        );
        provider = new TokenProvider(jwtProperties);
    }

    @Test
    void issueAccessTokenAndParseTest() {
        // given
        Long memberId = 100L;
        Long companyId = 1L;
        MemberRole role = MemberRole.ROLE_MEMBER;

        // when
        String token = provider.issueAccessToken(memberId, companyId, role);
        log.info("token = {}", token);
        AccessTokenClaims claims = provider.parseAccessToken(token);

        String token2 = provider.issueAccessToken(memberId, companyId, role);
        log.info("token2 = {}", token2);

        // then
        assertThat(claims.memberId()).isEqualTo(memberId);
        assertThat(claims.companyId()).isEqualTo(companyId);
        assertThat(claims.role()).isEqualTo(role);


    }

    @Test
    void exceptionIfExpiredAccessTokenTest() throws InterruptedException {
        JwtProperties jwtProperties = new JwtProperties(
                ISSUER,
                Base64.getEncoder().encodeToString(SECRET.getBytes()),
                Duration.ofSeconds(1),
                Duration.ofDays(14)
        );
        TokenProvider tokenProvider = new TokenProvider(jwtProperties);
        String token = tokenProvider.issueAccessToken(1L, 1L, MemberRole.ROLE_MEMBER);

        Thread.sleep(1500);

        Assertions.assertThatThrownBy(() -> tokenProvider.parseAccessToken(token))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode").isEqualTo(TokenErrorCode.ACCESS_TOKEN_EXPIRED);
    }

    @Test
    void exceptionIfInvalidTest() {
        String token = provider.issueAccessToken(100L, 1L, MemberRole.ROLE_MEMBER);
        String tampered = token.substring(0, token.length() -5) + "AAAAA";
        log.info("token = {}, tampered = {}", token, tampered);
        Assertions.assertThatThrownBy(() -> provider.parseAccessToken(tampered))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode").isEqualTo(TokenErrorCode.ACCESS_TOKEN_INVALID);

         // eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzbmFjazI0LWlkZW50aXR5Iiwic3ViIjoiMTAwIiwiY29tcGFueUlkIjoxLCJyb2xlIjoiUk9MRV9NRU1CRVIiLCJpYXQiOjE3Nzg1NjExNTksImV4cCI6MTc3ODU2Mjk1OX0.l9QH3lHycGGfxp2DQ1WCIH9ow7mk3xIVYh-HHSD_Rfc
         // eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzbmFjazI0LWlkZW50aXR5Iiwic3ViIjoiMTAwIiwiY29tcGFueUlkIjoxLCJyb2xlIjoiUk9MRV9NRU1CRVIiLCJpYXQiOjE3Nzg1NjExNTksImV4cCI6MTc3ODU2Mjk1OX0.l9QH3lHycGGfxp2DQ1WCIH9ow7mk3xIVYh-HHSAAAAA
    }

    @Test
    void issueRefreshTokenTest() {
        String refreshToken = provider.issueRefreshToken();
        log.info("refreshToken = {}", refreshToken);
        assertThat(refreshToken).hasSize(43);
        assertThat(refreshToken).matches("^[A-Za-z0-9_-]+$");
    }

}