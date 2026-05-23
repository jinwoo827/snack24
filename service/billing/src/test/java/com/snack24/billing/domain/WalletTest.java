package com.snack24.billing.domain;

import com.snack24.billing.exception.BillingErrorCode;
import com.snack24.billing.exception.BillingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Nested
    @DisplayName("credit (잔액증가)")
    class Credit {
        @Test
        void 정상_금액일때_balacne_증가() {
            // given
            Wallet wallet = Wallet.openFor(1L, 100L);
            BigDecimal chargeAmount = new BigDecimal("10000.00");
            // when
            wallet.credit(chargeAmount);

            // then
            Assertions.assertThat(wallet.getBalance())
                    .isEqualTo(chargeAmount);
        }

        @Test
        void 누적_증가() {
            // given
            Wallet wallet = Wallet.openFor(1L, 100L);

            // when
            wallet.credit(new BigDecimal("10000"));
            wallet.credit(new BigDecimal("20000"));

            // then
            Assertions.assertThat(wallet.getBalance()).isEqualByComparingTo("30000");
        }

        @Test
        void 음수_금액이면_예외() {
            // given
            Wallet wallet = Wallet.openFor(1L, 100L);

            // when then
            Assertions.assertThatThrownBy(() -> wallet.credit(new BigDecimal("-100000")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 금액이_0원이면_예외() {
            //given
            Wallet wallet = Wallet.openFor(1L, 1L);

            // when then
            Assertions.assertThatThrownBy(() -> wallet.credit(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void 금액이_null이면_예외() {
            // given
            Wallet wallet = Wallet.openFor(1L, 1L);

            // when then
            Assertions.assertThatThrownBy(() -> wallet.credit(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("debit (잔액 차감)")
    class Debit {
        @Test
        void 잔액이_충분하면_차감() {
            Wallet emptyWallet = getEmptyWallet();
            increaseBalance(emptyWallet, new BigDecimal("10000"));
            decreaseBalance(emptyWallet, new BigDecimal("3000"));
            emptyWallet.debit(new BigDecimal("5000"));
            Assertions.assertThat(emptyWallet.getBalance()).isEqualByComparingTo("2000");
        }

        @Test
        void 잔액까지_차감하면_잔액은0() {
            Wallet emptyWallet = getEmptyWallet();
            increaseBalance(emptyWallet, new BigDecimal("10000"));
            decreaseBalance(emptyWallet, new BigDecimal("10000"));
            Assertions.assertThat(emptyWallet.getBalance()).isEqualByComparingTo("0");
        }

        @Test
        void 잔액이_부족하면_INSUFFICIENT_BALANCE_예외() {
            Wallet emptyWallet = getEmptyWallet();
            Assertions.assertThatThrownBy(() -> decreaseBalance(emptyWallet, new BigDecimal("1000")))
                    .isInstanceOf(BillingException.class)
                    .extracting("errorCode").isEqualTo(BillingErrorCode.INSUFFICIENT_BALANCE);
        }
    }

    @Test
    @DisplayName("Wallet openFor")
    void walletOpenForTest() {
        Long companyId = 1L;
        Long walletId = 100L;
        Wallet wallet = Wallet.openFor(walletId, companyId);
        Assertions.assertThat(wallet.getWalletId()).isEqualTo(walletId);
        Assertions.assertThat(wallet.getCompanyId()).isEqualTo(companyId);
        Assertions.assertThat(wallet.getBalance()).isEqualByComparingTo("0");

    }

    private Wallet getEmptyWallet() {
        return Wallet.openFor(1L, 1L);
    }

    private void increaseBalance(Wallet wallet, BigDecimal amount) {
        wallet.credit(amount);
    }

    private void decreaseBalance(Wallet wallet, BigDecimal amount) {
        wallet.debit(amount);
    }
}