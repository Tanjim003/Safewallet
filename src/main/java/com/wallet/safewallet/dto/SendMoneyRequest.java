package com.wallet.safewallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SendMoneyRequest {

    @NotBlank(message = "Recipient phone is required")
    private String recipientPhone;

    @Positive(message = "Amount must be greater than 0 ")
    private BigDecimal amount;

    private String note;
}
