package com.snack24.catalog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("snack24 catalog Service API")
                        .description("catalog (port 8002)")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("X-Company-Id", apiKeyHeader("X-Company-Id"))
                        .addSecuritySchemes("X-Member-Id",  apiKeyHeader("X-Member-Id"))
                        .addSecuritySchemes("X-Member-Role", apiKeyHeader("X-Member-Role")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-Company-Id")
                        .addList("X-Member-Id")
                        .addList("X-Member-Role"));
    }

    private SecurityScheme apiKeyHeader(String headerName) {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(headerName);
    }
}
