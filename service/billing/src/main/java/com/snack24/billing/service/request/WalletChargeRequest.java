package com.snack24.billing.service.request;

import java.math.BigDecimal;

public record WalletChargeRequest(
        BigDecimal amount,
        String idempotencyKey
) {
}
