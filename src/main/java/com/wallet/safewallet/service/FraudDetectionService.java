package com.wallet.safewallet.service;

import com.wallet.safewallet.entity.DailyTransferTotal;
import com.wallet.safewallet.exception.DailyLimitExceededException;
import com.wallet.safewallet.repository.DailyTransferTotalRepository;
import com.wallet.safewallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final DailyTransferTotalRepository dailyTransferTotalRepository;
    private final TransactionRepository transactionRepository;

    @Value("${app.fraud.daily-transfer-limit:30000}")
    private BigDecimal dailyTransferLimit;

    //hard blog if amount > 30000 in daily transfer

    public void checkDailyLimit(Long walletId, BigDecimal amount){
        LocalDate today = LocalDate.now();
        DailyTransferTotal dailyTotal = dailyTransferTotalRepository
                .findByWalletIdAndDate(walletId, today)
                .orElse(DailyTransferTotal.builder()
                        .walletId(walletId)
                        .date(today)
                        .totalSent(BigDecimal.ZERO)
                        .build());

        BigDecimal newTotal = dailyTotal.getTotalSent().add(amount);
        if(newTotal.compareTo(dailyTransferLimit) > 0){
            throw new DailyLimitExceededException(
                    "Daily Transfer limit exceeded. Max: " + dailyTransferLimit +
                            ", attemped: " + newTotal);
        }

    }

    //softflag = flag true if suspicious and return flag reason

    public String analyzeTransaction(Long walletId, BigDecimal amount){
        if(amount.compareTo(new BigDecimal("10000")) > 0 ){
            return "Suspicious: Single Transfer above 10,000";
        }

        long recentCount = transactionRepository.countBySenderIdAndCreatedAtAfter(
                walletId, LocalDateTime.now().minusHours(1));
        if(recentCount > 10){
            return "Suspicious: more than 10 transfers in the last hour";
        }
        return null;
    }

    // update daily running total after a successful transfer

    public void updateDailyTotal(Long walletId, BigDecimal amount){
        LocalDate today = LocalDate.now();
        DailyTransferTotal dailyTotal = dailyTransferTotalRepository
                .findByWalletIdAndDate(walletId, today)
                .orElse(DailyTransferTotal.builder()
                        .walletId(walletId)
                        .date(today)
                        .totalSent(BigDecimal.ZERO)
                        .build());
        dailyTotal.setTotalSent(dailyTotal.getTotalSent().add(amount));
        dailyTransferTotalRepository.save(dailyTotal);
    }



}
