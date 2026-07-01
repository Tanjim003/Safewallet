package com.wallet.safewallet.service;

import com.wallet.safewallet.entity.User;
import com.wallet.safewallet.entity.Wallet;
import com.wallet.safewallet.repository.UserRepository;
import com.wallet.safewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public BigDecimal getBalance(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();

        User user = userRepository.findByPhone(phone)
                .orElseThrow(()-> new RuntimeException("User not found !"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(()-> new RuntimeException("Wallet not found !"));

        return wallet.getBalance();
    }

}
