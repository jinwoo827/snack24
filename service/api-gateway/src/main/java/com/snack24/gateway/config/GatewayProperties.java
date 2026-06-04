package com.snack24.gateway.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "snack24.gateway")
public record GatewayProperties(
        @NotNull
        List<String> permitAllPaths
) {
}
