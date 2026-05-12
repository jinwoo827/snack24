package com.snack24.identity.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "snack24.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String secret,
        @NotNull Duration accessTokenValidity,
        @NotNull Duration refreshTokenValidity
        ) {
}
