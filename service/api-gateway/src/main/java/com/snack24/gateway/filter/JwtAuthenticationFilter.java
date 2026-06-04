package com.snack24.gateway.filter;

import com.snack24.gateway.config.GatewayProperties;
import com.snack24.gateway.exception.GatewayErrorCode;
import com.snack24.gateway.exception.GatewayException;
import com.snack24.gateway.jwt.AccessTokenClaims;
import com.snack24.gateway.jwt.TokenValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final TokenValidator tokenValidator;
    private final GatewayProperties gatewayProperties;
    private final ErrorResponseWriter errorResponseWriter;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        // 화이트리스트 - jwt
        if (isPermitAll(method, path)) {
            log.debug("[Gateway] permit-all path: {} {}", method, path);
            return chain.filter(exchange);
        }

        // header : Authorization: Bearer ...
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return errorResponseWriter.writeUnauthorized(
                    exchange,
                    GatewayErrorCode.AUTH_REQUIRED.getCode(),
                    GatewayErrorCode.AUTH_REQUIRED.getDefaultMessage()
            );
        }
        String token = authHeader.substring(7);

        // token
        AccessTokenClaims claims;
        try {
            claims = tokenValidator.validate(token);
        } catch (GatewayException e) {
            return errorResponseWriter.writeUnauthorized(
                    exchange,
                    e.getErrorCode().getCode(),
                    e.getErrorCode().getDefaultMessage()
            );
        }

        // 신뢰 헤더 주입
        ServerHttpRequest mutated = request.mutate()
                .header("X-Company-Id", String.valueOf(claims.companyId()))
                .header("X-Member-Id", String.valueOf(claims.memberId()))
                .header("X-Member-Role", claims.role())
                .build();


        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPermitAll(String method, String path) {
        return gatewayProperties.permitAllPaths().stream()
                .anyMatch(pattern -> {
                    if (pattern.contains(":") && !pattern.startsWith("/")) {
                        String[] parts = pattern.split(":", 2);
                        return parts[0].equalsIgnoreCase(method) && pathMatcher.match(parts[1], path);
                    }
                    return pathMatcher.match(pattern, path);
                });
    }
}
