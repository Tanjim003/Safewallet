package com.wallet.safewallet.payment;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FakePaymentProvider implements PaymentProvider {


    @Override
    public PaymentResponse process(PaymentRequest request) {
       return PaymentResponse.builder()
               .success(true)
               .providerTransactionId("fake-" + UUID.randomUUID())
               .status("SUCCESS")
               .message("Mock Payment Successful")
               .build();
    }
}
