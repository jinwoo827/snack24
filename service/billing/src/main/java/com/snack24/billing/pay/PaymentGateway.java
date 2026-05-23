package com.snack24.billing.pay;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentResult charge(Long companyId, BigDecimal amount, String idempotencyKey);
}
