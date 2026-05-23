package com.snack24.billing.pay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.Key;
import java.util.UUID;

@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway{
    @Override
    public PaymentResult charge(Long companyId, BigDecimal amount, String idempotencyKey) {
        log.info("[MockPG] charge companyId = {}, amount = {}, key = {}", companyId, amount, idempotencyKey);
        return PaymentResult.ok("MOCK-" + UUID.randomUUID());
    }
}
