package com.snack24.identity.auth;

import com.snack24.identity.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
public class TokenProvider {

    private final JwtProperties props;
    private final SecretKey signinKey;
    private final SecureRandom random = new SecureRandom();

    public TokenProvider(JwtProperties props) {
        this.props = props;
        byte[] keyBytes = Decoders.BASE64.decode(props.secret());
        this.signinKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String issueAccessToken(Long memberId, Long companyId, MemberRole role) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.accessTokenValidity());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(props.issuer())
                .subject(String.valueOf(memberId))
                .claim("companyId", companyId)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signinKey, Jwts.SIG.HS256)
                .compact();
    }

    public AccessTokenClaims parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signinKey)
                    .requireIssuer(props.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new AccessTokenClaims(
                    Long.parseLong(claims.getSubject()),
                    claims.get("companyId", Long.class),
                    MemberRole.valueOf(claims.get("role", String.class)),
                    claims.getExpiration().toInstant()
            );
        } catch (ExpiredJwtException e) {
            throw new TokenException(TokenErrorCode.ACCESS_TOKEN_EXPIRED, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenException(TokenErrorCode.ACCESS_TOKEN_INVALID, e);
        }
    }

    public String issueRefreshToken() {
        byte[] bytes = new byte[32]; // 256bit
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Duration getRefreshTokenValidity() {
        return props.refreshTokenValidity();
    }

    public Duration getAccessTokenValidity() {
        return props.accessTokenValidity();
    }
}
