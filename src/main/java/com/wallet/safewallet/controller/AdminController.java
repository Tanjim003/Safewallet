package com.wallet.safewallet.controller;

import com.wallet.safewallet.dto.ApiResponse;
import com.wallet.safewallet.payment.PaymentRequest;
import com.wallet.safewallet.service.PaymentService;
import com.wallet.safewallet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/wallet/topUp")
    public ResponseEntity<ApiResponse<Void>> topUp(
            @RequestParam String targetPhone,
            @Valid @RequestBody PaymentRequest request
            ){
        Long walletId = userService.getWalletIdByPhone(targetPhone);
        paymentService.topUp(walletId, request);
        return ResponseEntity.ok(ApiResponse.ok("Top-Up successful"));

    }
}
