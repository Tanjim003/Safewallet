package com.wallet.safewallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionHistoryItem {

    private Long transactionId;
    private String senderPhone;
    private String receiverPhone;
    private BigDecimal amount;
    private String transactionType;          // TRANSFER, TOP_UP, WITHDRAWAL
    private String direction; // send or received (only meaningful for transfer)
    private String status;
    private String note;
    private LocalDateTime createdAt;

    public TransactionHistoryItem(Long transactionId, String senderPhone, String receiverPhone,
                                  BigDecimal amount, String transactionType, String status,
                                  String note, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.senderPhone = senderPhone;
        this.receiverPhone = receiverPhone;
        this.amount = amount;
        this.transactionType = transactionType;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
    }

}
