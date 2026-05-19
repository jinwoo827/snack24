package com.snack24.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.snack24.catalog", "com.snack24.common.outboxrelay"})
@EnableJpaRepositories(basePackages = {"com.snack24.catalog", "com.snack24.common.outboxrelay"})
@ComponentScan(basePackages = {"com.snack24.catalog", "com.snack24.common.outboxrelay"})
@ConfigurationPropertiesScan(basePackages = "com.snack24.catalog")
public class CatalogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
