package com.wallet.safewallet.service;

import com.wallet.safewallet.dto.TransactionHistoryItem;
import com.wallet.safewallet.entity.User;
import com.wallet.safewallet.entity.Wallet;
import com.wallet.safewallet.repository.TransactionRepository;
import com.wallet.safewallet.repository.UserRepository;
import com.wallet.safewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public Page<TransactionHistoryItem> getTransactionHistory(int page , int size){

        String phone = SecurityContextHolder.getContext().getAuthentication().getName();

        User  user = userRepository.findByPhone(phone)
                .orElseThrow(()-> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(()-> new RuntimeException("Wallet not found"));

        PageRequest pageRequest = PageRequest.of(page, size);
        return transactionRepository.findTransactionHistoryByWalletId(wallet.getId(),
                pageRequest);






    }
}
