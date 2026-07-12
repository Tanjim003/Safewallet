package com.wallet.safewallet.service;

import com.wallet.safewallet.dto.SendMoneyRequest;
import com.wallet.safewallet.entity.Transaction;
import com.wallet.safewallet.entity.TransactionType;
import com.wallet.safewallet.entity.User;
import com.wallet.safewallet.entity.Wallet;
import com.wallet.safewallet.exception.DailyLimitExceededException;
import com.wallet.safewallet.exception.DuplicateTransactionException;
import com.wallet.safewallet.exception.InsufficientFundsException;
import com.wallet.safewallet.repository.TransactionRepository;
import com.wallet.safewallet.repository.UserRepository;
import com.wallet.safewallet.repository.WalletRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final MetricsService metricsService;
    private final MeterRegistry meterRegistry;

    public BigDecimal getBalance(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();

        User user = userRepository.findByPhone(phone)
                .orElseThrow(()-> new RuntimeException("User not found !"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(()-> new RuntimeException("Wallet not found !"));

        return wallet.getBalance();
    }

    @Transactional
    public void sendMoney(String senderPhone, SendMoneyRequest request) {
        // Start timing the transfer
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            User sender = userRepository.findByPhone(senderPhone)
                    .orElseThrow(() -> new RuntimeException("Sender not found !"));
            Wallet senderWallet = walletRepository.findByUserIdWithLock(sender.getId())
                    .orElseThrow(() -> new RuntimeException("Sender wallet not found !"));

            User reciver = userRepository.findByPhone(request.getRecipientPhone())
                    .orElseThrow(() -> new RuntimeException("Recipient not found !"));
            Wallet reciverWallet = walletRepository.findByUserIdWithLock(reciver.getId())
                    .orElseThrow(() -> new RuntimeException("Recipient wallet not found !"));

            if (senderWallet.getIsFrozen() || reciverWallet.getIsFrozen()) {
                throw new RuntimeException("One of the wallets is frozen !");
            }

            if (senderPhone.equals(request.getRecipientPhone())) {
                throw new RuntimeException("Cannot transfer to yourself !");
            }

            if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException("Insufficient balance");
            }

            // Hard fraud check (throws DailyLimitExceededException if exceeded)
            fraudDetectionService.checkDailyLimit(senderWallet.getId(), request.getAmount());

            // Idempotency key
            String idempotencyKey;
            if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
                idempotencyKey = request.getIdempotencyKey();
            } else {
                idempotencyKey = generateIdempotencyKey(sender.getId(), reciver.getId(), request.getAmount());
            }

            if (transactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
                throw new DuplicateTransactionException("Transaction is already processed");
            }

            // Perform transfer
            senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
            walletRepository.save(senderWallet);

            reciverWallet.setBalance(reciverWallet.getBalance().add(request.getAmount()));
            walletRepository.save(reciverWallet);

            // Build transaction
            Transaction transaction = Transaction.builder()
                    .senderId(senderWallet.getId())
                    .receiverId(reciverWallet.getId())
                    .amount(request.getAmount())
                    .transactionType(TransactionType.TRANSFER)
                    .idempotencyKey(idempotencyKey)
                    .status("SUCCESS")
                    .note(request.getNote())
                    .isFlagged(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Soft fraud flagging
            String flagReason = fraudDetectionService.analyzeTransaction(senderWallet.getId(), request.getAmount());
            if (flagReason != null) {
                transaction.setIsFlagged(true);
                transaction.setFlagReason(flagReason);
                metricsService.fraudFlagCounter.increment();
            }

            // Save transaction and update daily totals
            transactionRepository.save(transaction);
            fraudDetectionService.updateDailyTotal(senderWallet.getId(), request.getAmount());

            // Increment success counter
            metricsService.transferCounter.increment();

        } catch (DailyLimitExceededException e) {
            metricsService.fraudBlockCounter.increment();
            throw e; // re-throw to trigger rollback
        } finally {
            sample.stop(metricsService.transferTimer);
        }
    }

    private String generateIdempotencyKey(Long senderId, Long reciverId, BigDecimal amount){
        long minute = System.currentTimeMillis() / 60_000;
        String raw = senderId + "-" + reciverId + "-" + amount + "-" + minute;
        return UUID.nameUUIDFromBytes(raw.getBytes()).toString();

    }

}
