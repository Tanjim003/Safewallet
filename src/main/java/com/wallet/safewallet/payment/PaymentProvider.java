package com.wallet.safewallet.payment;

public interface PaymentProvider {
    PaymentResponse process(PaymentRequest request);
}
