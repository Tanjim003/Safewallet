package com.wallet.safewallet.repository;

import com.wallet.safewallet.entity.DailyTransferTotal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTransferTotalRepository extends JpaRepository<DailyTransferTotal, Long> {
    Optional<DailyTransferTotal> findByWalletIdAndDate(Long walletId, LocalDate date);

}
