package com.snack24.billing.service.response;

import com.snack24.billing.domain.Wallet;

import java.math.BigDecimal;

public record WalletChargeResponse(
        Long walletId,
        Long companyId,
        BigDecimal balance
) {
    public static WalletChargeResponse from(Wallet wallet) {
        return new WalletChargeResponse(
                wallet.getWalletId(),
                wallet.getCompanyId(),
                wallet.getBalance()
        );
    }
}
