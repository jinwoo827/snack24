package com.snack24.billing.pay;

public record PaymentResult(
        boolean success,
        String pgTransactionId,
        String failureReason
) {
    public static PaymentResult ok(String pgTxId) {
        return new PaymentResult(true, pgTxId, null);
    }

    public static PaymentResult fail(String reason) {
        return new PaymentResult(false, null, reason);
    }
}
