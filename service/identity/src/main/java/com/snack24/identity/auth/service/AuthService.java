package com.snack24.identity.auth.service;

import com.snack24.identity.auth.RefreshTokenValue;
import com.snack24.identity.auth.TokenProvider;
import com.snack24.identity.auth.repository.RefreshTokenRepository;
import com.snack24.identity.auth.service.dto.request.LoginRequest;
import com.snack24.identity.auth.service.dto.request.LogoutRequest;
import com.snack24.identity.auth.service.dto.request.RefreshRequest;
import com.snack24.identity.auth.service.dto.response.LoginResponse;
import com.snack24.identity.domain.Member;
import com.snack24.identity.domain.MemberStatus;
import com.snack24.identity.exception.IdentityErrorCode;
import com.snack24.identity.exception.IdentityException;
import com.snack24.identity.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(loginRequest.password(), member.getPasswordHash())) {
            throw new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS);
        }

        return issueToken(member);
    }

    public LoginResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        /*if (refreshTokenRepository.isRevoked(refreshToken)) {
            refreshTokenRepository.find(refreshToken).ifPresent(v -> {
                log.warn("[Auth] refresh token reuse detected. memberId = {}, revoking all sessions", v.memberId());
                refreshTokenRepository.revokeAll(v.memberId());
            });
            throw new IdentityException(IdentityErrorCode.INVALID_REFRESH_TOKEN);
        }*/
        Optional<Long> revokedMemberId = refreshTokenRepository.findRevokedMemberId(refreshToken);
        if (revokedMemberId.isPresent()) {
            Long memberId = revokedMemberId.get();
            log.warn("[Auth] refresh token reuse detected. memberId = {}, revoking all sessions", memberId);
            refreshTokenRepository.revokeAll(memberId);
            throw new IdentityException(IdentityErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshTokenValue refreshTokenValue = refreshTokenRepository.find(refreshToken)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_REFRESH_TOKEN));

        Member member = memberRepository.findById(refreshTokenValue.memberId())
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new IdentityException(IdentityErrorCode.INVALID_CREDENTIALS));

        refreshTokenRepository.delete(refreshToken, member.getMemberId());
        refreshTokenRepository.markedRevoked(refreshToken, member.getMemberId(), tokenProvider.getRefreshTokenValidity());

        return issueToken(member);
    }

    public void logout(LogoutRequest logoutRequest) {
        refreshTokenRepository.find(logoutRequest.refreshToken())
                .ifPresent(refreshTokenValue -> refreshTokenRepository.delete(logoutRequest.refreshToken(), refreshTokenValue.memberId()));
    }

    private LoginResponse issueToken(Member member) {
        String accessToken = tokenProvider.issueAccessToken(member.getMemberId(), member.getCompanyId(), member.getRole());
        String refreshToken = tokenProvider.issueRefreshToken();
        refreshTokenRepository.save(refreshToken, member.getMemberId(), tokenProvider.getRefreshTokenValidity());

        return new LoginResponse(
                accessToken,
                refreshToken,
                member.getMemberId(),
                member.getCompanyId(),
                member.getRole(),
                tokenProvider.getAccessTokenValidity().toSeconds()
        );

    }
}
