package com.snack24.gateway.jwt;

import com.snack24.gateway.config.JwtProperties;
import com.snack24.gateway.exception.GatewayErrorCode;
import com.snack24.gateway.exception.GatewayException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class TokenValidator {
    private final SecretKey signingKey;
    private final String issuer;

    public TokenValidator(JwtProperties props) {
        byte[] keyBytes = Decoders.BASE64.decode(props.secret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = props.issuer();
    }

    public AccessTokenClaims validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new AccessTokenClaims(
                    Long.parseLong(claims.getSubject()),
                    claims.get("companyId", Long.class),
                    claims.get("role", String.class)
            );
        } catch (ExpiredJwtException e) {
            throw new GatewayException(GatewayErrorCode.ACCESS_TOKEN_EXPIRED, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new GatewayException(GatewayErrorCode.ACCESS_TOKEN_INVALID, e);
        }

    }
}
