package com.wallet.safewallet.service;

import com.wallet.safewallet.entity.Transaction;
import com.wallet.safewallet.entity.TransactionType;
import com.wallet.safewallet.entity.Wallet;
import com.wallet.safewallet.exception.DuplicateTransactionException;
import com.wallet.safewallet.payment.PaymentProvider;
import com.wallet.safewallet.payment.PaymentRequest;
import com.wallet.safewallet.payment.PaymentResponse;
import com.wallet.safewallet.repository.TransactionRepository;
import com.wallet.safewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentProvider paymentProvider;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;


    @Transactional
    public Transaction topUp(Long walletId, PaymentRequest request){
        String idempotencyKey = resolveIdempotencyKey(request.getIdempotencyKey());
        checkDuplicate(idempotencyKey);

        Transaction tx = buildTransaction(null, walletId, request.getAmount(),
                TransactionType.TOP_UP, idempotencyKey, request.getNote(), "PENDING");
        transactionRepository.save(tx);

        PaymentResponse response = paymentProvider.process(request);
        if(response.isSuccess()) {
            creditWallet(walletId, request.getAmount());
            tx.setStatus("SUCCESS");
        } else {
            tx.setStatus("FAILED");
        }
        transactionRepository.save(tx);
        return tx;
    }


    @Transactional
    public Transaction withdraw(Long walletId, PaymentRequest request){
        String idempotencyKey = resolveIdempotencyKey(request.getIdempotencyKey());
        checkDuplicate(idempotencyKey);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));
        if(wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        Transaction tx = buildTransaction(null, walletId, request.getAmount(),
                TransactionType.WITHDRAWAL, idempotencyKey, request.getNote(), "PENDING");
        transactionRepository.save(tx);

        PaymentResponse response = paymentProvider.process(request);
        if(response.isSuccess()) {
            debitWallet(walletId, request.getAmount());
            tx.setStatus("SUCCESS");
        } else {
            tx.setStatus("FAILED");
        }
        transactionRepository.save(tx);
        return tx;
    }

    // helper
    private String resolveIdempotencyKey(String clientKey){
        if(clientKey != null && !clientKey.isBlank()) return clientKey;
        return UUID.randomUUID().toString();
    }

    private void checkDuplicate(String idempotencyKey){
        if(transactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new DuplicateTransactionException("Transfer already processed");
        }

    }

    private Transaction buildTransaction(Long senderId, Long recieverId, BigDecimal amount,
                                         TransactionType type, String idempotencyKey,
                                         String note , String status) {
        return Transaction.builder()
                .senderId(senderId)
                .receiverId(recieverId)
                .amount(amount)
                .transactionType(type)
                .idempotencyKey(idempotencyKey)
                .status(status)
                .note(note)
                .isFlagged(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void creditWallet(Long walletId, BigDecimal amount){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    private void debitWallet(Long walletId, BigDecimal amount){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

}
