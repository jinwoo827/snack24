package com.snack24.catalog.config;

import com.snack24.common.snowflake.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdConfig {

    @Bean
    public Snowflake snowflake() {
        return new Snowflake();
    }
}
