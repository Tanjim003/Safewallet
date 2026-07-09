package com.wallet.safewallet.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private boolean success;
    private String providerTransactionId;
    private String status;
    private String message;
}
