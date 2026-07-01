package com.wallet.safewallet.controller;

import com.wallet.safewallet.dto.ApiResponse;
import com.wallet.safewallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
