package com.wallet.safewallet.controller;

import com.wallet.safewallet.dto.ApiResponseDTO;
import com.wallet.safewallet.dto.TransactionHistoryItem;
import com.wallet.safewallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<Page<TransactionHistoryItem>>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size ){
        Page<TransactionHistoryItem> history = transactionService.getTransactionHistory(page, size);

        return ResponseEntity.ok(ApiResponseDTO.ok("Transaction history fetched", history));
    }

}
