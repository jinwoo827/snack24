package com.snack24.order.client;

import com.snack24.order.client.dto.ProductPrice;
import com.snack24.order.exception.OrderErrorCode;
import com.snack24.order.exception.OrderException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class CatalogClient {
    private final RestClient restClient;

    public CatalogClient(
            RestClient.Builder builder,
            @Value("${snack24.client.catalog.base-url}") String baseUrl,
            @Value("${snack24.client.catalog.connect-timeout}")Duration connectTimeout,
            @Value("${snack24.client.catalog.read-timeout}") Duration readTimeout
            ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public ProductPrice getPrice(Long productId, Long companyId, Long memberId, String role) {
        return restClient.get()
                .uri("/v1/products/{productId}", productId)
                .header("X-Company-Id", String.valueOf(companyId))
                .header("X-Member-Id", String.valueOf(memberId))
                .header("X-Member-Role", role)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new OrderException(OrderErrorCode.PRODUCT_NOT_FOUND);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new OrderException(OrderErrorCode.CATALOG_UNAVAILABLE);
                })
                .body(ProductPrice.class);
    }
}
