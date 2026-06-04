package com.snack24.gateway.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "snack24.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String secret
) {
}
