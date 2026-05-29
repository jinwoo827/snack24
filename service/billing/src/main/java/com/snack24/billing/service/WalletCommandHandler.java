package com.snack24.billing.service;

import com.snack24.billing.domain.Wallet;
import com.snack24.billing.domain.WalletTransaction;
import com.snack24.billing.exception.BillingErrorCode;
import com.snack24.billing.exception.BillingException;
import com.snack24.billing.repository.WalletRepository;
import com.snack24.billing.repository.WalletTransactionRepository;
import com.snack24.common.event.EventType;
import com.snack24.common.event.payload.DebitWalletCommandPayload;
import com.snack24.common.event.payload.WalletDebitFailedPayload;
import com.snack24.common.event.payload.WalletDebitedPayload;
import com.snack24.common.outboxrelay.outbox.event.OutboxEventPublisher;
import com.snack24.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class WalletCommandHandler {
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ProcessedMessageService processedMessageService;
    private final OutboxEventPublisher outboxEventPublisher;
    private final Snowflake snowflake;

    // 잔액 차감
    @Transactional
    public void handleDebit(DebitWalletCommandPayload payload) {

        // 이벤트 멱등성 체크
        if (!processedMessageService.markIfFirst(payload.getSagaId(), EventType.DEBIT_WALLET_COMMAND)) {
            return;
        }

        // wallet 검증
        Optional<Wallet> walletOpt = walletRepository.findByCompanyId(payload.getCompanyId());
        if (walletOpt.isEmpty()) {
            publishDebitFailed(payload, "wallet not found");
            return;
        }

        Wallet wallet = walletOpt.get();

        int affected = walletRepository.debitIfSufficient(payload.getCompanyId(), payload.getAmount());

        if (affected == 1) {
            walletTransactionRepository.save(
                    WalletTransaction.debit(
                            snowflake.nextId(),
                            wallet.getWalletId(),
                            payload.getAmount(),
                            payload.getOrderId(),
                            payload.getSagaId()
                    )
            );

            publishDebited(payload);
        } else {
            publishDebitFailed(payload, "insufficient balance");
        }

    }

    private void publishDebited(DebitWalletCommandPayload payload) {
        outboxEventPublisher.publish(
                EventType.WALLET_DEBITED,
                WalletDebitedPayload.builder()
                        .sagaId(payload.getSagaId())
                        .orderId(payload.getOrderId())
                        .companyId(payload.getCompanyId())
                        .amount(payload.getAmount())
                        .build(),
                payload.getSagaId()
        );
    }

    private void publishDebitFailed(DebitWalletCommandPayload payload, String reason) {
        outboxEventPublisher.publish(
                EventType.WALLET_DEBIT_FAILED,
                WalletDebitFailedPayload.builder()
                        .sagaId(payload.getSagaId())
                        .orderId(payload.getOrderId())
                        .reason(reason)
                        .build(),
                payload.getSagaId()
        );
    }
}
