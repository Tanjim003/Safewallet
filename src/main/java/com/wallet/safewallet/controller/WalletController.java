package com.wallet.safewallet.controller;

import com.wallet.safewallet.dto.ApiResponse;
import com.wallet.safewallet.dto.SendMoneyRequest;
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

}
