package com.wallet.safewallet.controller;

import com.wallet.safewallet.dto.ApiResponse;
import com.wallet.safewallet.dto.SendMoneyRequest;
import com.wallet.safewallet.entity.Transaction;
import com.wallet.safewallet.payment.PaymentRequest;
import com.wallet.safewallet.service.PaymentService;
import com.wallet.safewallet.service.UserService;
import com.wallet.safewallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final PaymentService paymentService;
    private final UserService userService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(){
        BigDecimal balance = walletService.getBalance();
        return ResponseEntity.ok(ApiResponse.ok("Balance fetched", balance));
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendMoney(@Valid @RequestBody SendMoneyRequest request){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String senderPhone = auth.getName();
        walletService.sendMoney(senderPhone, request);
        return ResponseEntity.ok(ApiResponse.ok("Transfer Successful"));

    }

    @PostMapping("/withDraw")
    public ResponseEntity<ApiResponse<Transaction>> withDraw(@Valid @RequestBody PaymentRequest request) {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        Long walletId = userService.getWalletIdByPhone(phone);
        Transaction tx = paymentService.withdraw(walletId, request);
        return ResponseEntity.ok(ApiResponse.ok("Withdrawal successful", tx));
    }

}
