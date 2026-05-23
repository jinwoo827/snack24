package com.snack24.billing.repository;

import com.snack24.billing.domain.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Testcontainers
@Slf4j
class WalletRepositoryTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11")
            .withDatabaseName("snack24_billing")
            .withUsername("root")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.name", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    WalletRepository walletRepository;
    @Autowired
    TestEntityManager em;

    private Long givenWalletWithBalance(Long walletId, Long companyId, String initialBalance) {
        Wallet wallet = Wallet.openFor(walletId, companyId);
        wallet.credit(new BigDecimal(initialBalance));
        em.persistAndFlush(wallet);
        em.clear();
        return companyId;
    }

    @BeforeEach
    void cleanUp() {
        walletRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("잔액이 충분하면 1 반환후 잔액 감소")
    void debit_sufficient() {
        Long companyId = givenWalletWithBalance(1L, 1L, "10000");
        int affected = walletRepository.debitIfSufficient(companyId, new BigDecimal("3000"));
        assertThat(affected).isEqualTo(1);
        Wallet wallet = walletRepository.findByCompanyId(companyId)
                .orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo("7000");
    }

    @Test
    @DisplayName("잔액이 차감 금액과 동일할, 결과는 0")
    void debit_exact() {
        Long companyId = givenWalletWithBalance(1L, 1L, "10000");
        int affected = walletRepository.debitIfSufficient(companyId, new BigDecimal("10000"));
        assertThat(affected).isEqualTo(1);
        Wallet wallet = walletRepository.findByCompanyId(companyId)
                .orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("잔액이 부족하, 차감되지 않는다")
    void debit_insufficient() {
        Long companyId = givenWalletWithBalance(1L, 1L, "10000");
        int affected = walletRepository.debitIfSufficient(companyId, new BigDecimal("20000"));
        assertThat(affected).isZero();
        Wallet wallet = walletRepository.findByCompanyId(companyId)
                .orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("존재하지 않는 회사 일때, 차감되지 않는다")
    void debit_nonexistent_company() {
        int affected = walletRepository.debitIfSufficient(1L, new BigDecimal("10000"));
        assertThat(affected).isZero();
    }

    @Test
    @DisplayName("잔액 증가")
    void credit_increase_balance() {
        Long companyId = givenWalletWithBalance(1L, 1L, "10000");
        int affected = walletRepository.credit(companyId, new BigDecimal("10000"));
        assertThat(affected).isEqualTo(1);
        assertThat(walletRepository.findByCompanyId(companyId).orElseThrow().getBalance())
                .isEqualByComparingTo("20000");
    }

   /* @Test
    @DisplayName("동시 차감 시에, 잔액은 음수가 될수 없다")
    void concurrent_debit_never_goes_negative() throws InterruptedException {
        Long companyId = givenWalletWithBalance(1L, 100L, "100");
        int concurrency = 3;
        BigDecimal each = new BigDecimal("50");

        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(concurrency);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < concurrency; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    int affected = walletRepository.debitIfSufficient(companyId, each);
                    if (affected == 1) successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        pool.shutdownNow();

        em.clear();
        Wallet finalWallet = walletRepository.findByCompanyId(companyId).orElseThrow();

        assertThat(successCount.get()).isEqualTo(2);
        assertThat(finalWallet.getBalance()).isEqualByComparingTo("0");
    }*/
}