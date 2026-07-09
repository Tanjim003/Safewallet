package com.wallet.safewallet.payment;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
public class PaymentRequest {

    @NonNull @Positive
    private BigDecimal amount;
    private String idempotencyKey;
    private String note;

}
