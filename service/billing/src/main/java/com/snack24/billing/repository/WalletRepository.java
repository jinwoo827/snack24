package com.snack24.billing.repository;

import com.snack24.billing.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByCompanyId(Long companyId);

    @Modifying(clearAutomatically = true)
    @Query("update Wallet w " +
            "   set w.balance = w.balance - :amount " +
            " where w.companyId = :companyId " +
            "   and w.balance >= :amount "
    )
    int debitIfSufficient(@Param("companyId") Long companyId,
                          @Param("amount") BigDecimal amount);

    @Modifying(clearAutomatically = true)
    @Query(
            "update Wallet w " +
                    "   set w.balance = w.balance + :amount " +
                    " where w.companyId = :companyId "
    )
    int credit(@Param("companyId") Long companyId,
               @Param("amount") BigDecimal amount);
}
