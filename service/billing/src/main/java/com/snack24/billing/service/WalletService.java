package com.snack24.billing.service;

import com.snack24.billing.domain.Wallet;
import com.snack24.billing.domain.WalletTransaction;
import com.snack24.billing.exception.BillingErrorCode;
import com.snack24.billing.exception.BillingException;
import com.snack24.billing.pay.PaymentGateway;
import com.snack24.billing.pay.PaymentResult;
import com.snack24.billing.repository.WalletRepository;
import com.snack24.billing.repository.WalletTransactionRepository;
import com.snack24.billing.service.response.WalletChargeResponse;
import com.snack24.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PaymentGateway paymentGateway;
    private final Snowflake snowflake;

    @Transactional
    public void debit(Long companyId, BigDecimal amount, Long sagaId, Long orderId) {
        Wallet wallet = walletRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BillingException(BillingErrorCode.WALLET_NOT_FOUND));

        int affected = walletRepository.debitIfSufficient(companyId, amount);
        if (affected == 0) {
            throw new BillingException(BillingErrorCode.INSUFFICIENT_BALANCE);
        }
        walletTransactionRepository.save(
                WalletTransaction.debit(
                        snowflake.nextId(),
                        wallet.getWalletId(),
                        amount,
                        orderId,
                        sagaId
                )
        );
    }

    @Transactional
    public WalletChargeResponse charge(Long companyId, BigDecimal amount, String idempotencyKey) {
        Wallet wallet = walletRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BillingException(BillingErrorCode.WALLET_NOT_FOUND));

        PaymentResult result = paymentGateway.charge(companyId, amount, idempotencyKey);
        if (!result.success()) {
            throw new BillingException(BillingErrorCode.PAYMENT_FAILED);
        }

        walletRepository.credit(companyId, amount);
        walletTransactionRepository.save(
                WalletTransaction.charge(
                        snowflake.nextId(),
                        wallet.getWalletId(),
                        amount,
                        "PG TX : " + result.pgTransactionId()
                )
        );

        return WalletChargeResponse.from(wallet);
    }

    public WalletChargeResponse get(Long companyId) {
        Wallet wallet = walletRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new BillingException(BillingErrorCode.WALLET_NOT_FOUND));
        return WalletChargeResponse.from(wallet);
    }
}
