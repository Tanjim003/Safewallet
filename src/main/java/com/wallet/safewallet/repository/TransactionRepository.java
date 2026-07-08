package com.wallet.safewallet.repository;

import com.wallet.safewallet.dto.TransactionHistoryItem;
import com.wallet.safewallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT t FROM Transaction t WHERE t.senderId = :walletId OR t.receiverId = :walletId ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletId(@Param("walletId") Long walletId, Pageable pageable);

    long countBySenderIdAndCreatedAtAfter(Long senderId, LocalDateTime after);

    @Query("SELECT t FROM Transaction t WHERE t.isFlagged = true ORDER BY t.createdAt DESC")
    Page<Transaction> findAllFlagged(Pageable pageable);

    @Query("SELECT new com.wallet.safewallet.dto.TransactionHistoryItem(" +
            "t.id, " +
            "COALESCE(senderUser.phone, 'SYSTEM'), " +
            "COALESCE(receiverUser.phone, 'SYSTEM'), " +
            "t.amount, " +
            "CAST(t.transactionType AS string), " +
            "t.status, " +
            "t.note, " +
            "t.createdAt) " +
            "FROM Transaction t " +
            "LEFT JOIN Wallet senderWallet ON t.senderId = senderWallet.id " +
            "LEFT JOIN User senderUser ON senderWallet.userId = senderUser.id " +
            "LEFT JOIN Wallet receiverWallet ON t.receiverId = receiverWallet.id " +
            "LEFT JOIN User receiverUser ON receiverWallet.userId = receiverUser.id " +
            "WHERE t.senderId = :walletId OR t.receiverId = :walletId " +
            "ORDER BY t.createdAt DESC")
    Page<TransactionHistoryItem> findTransactionHistoryByWalletId(
            @Param("walletId") Long walletId, Pageable pageable);
}
