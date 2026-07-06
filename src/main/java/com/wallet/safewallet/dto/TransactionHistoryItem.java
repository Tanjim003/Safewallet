package com.wallet.safewallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TransactionHistoryItem {

    private Long transactionId;
    private String senderPhone;
    private String receiverPhone;
    private BigDecimal amount;
    private String type;          // TRANSFER, TOP_UP, WITHDRAWAL
    private String status;
    private String note;
    private LocalDateTime createdAt;

}
