package com.wallet.safewallet.service;

import com.wallet.safewallet.dto.SendMoneyRequest;
import com.wallet.safewallet.entity.User;
import com.wallet.safewallet.entity.Wallet;
import com.wallet.safewallet.exception.InsufficientFundsException;
import com.wallet.safewallet.repository.UserRepository;
import com.wallet.safewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void sendMoney(String senderPhone, SendMoneyRequest request){
        User sender = userRepository.findByPhone(senderPhone)
                .orElseThrow(()-> new RuntimeException("Sender not found !"));
        Wallet senderWallet = walletRepository.findByUserId(sender.getId())
                .orElseThrow(()-> new RuntimeException("Sender wallet not found !"));

        User reciver = userRepository.findByPhone(request.getRecipientPhone())
                .orElseThrow(()-> new RuntimeException("Recipient not found !"));
        Wallet reciverWallet = walletRepository.findByUserId(reciver.getId())
                .orElseThrow(()-> new RuntimeException("Recipient wallet not found !"));

        if(senderWallet.getBalance().compareTo(request.getAmount()) < 0){
            throw new InsufficientFundsException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(senderWallet);

        if(request.getAmount().intValue() > 500){
            throw new RuntimeException("Simulated fraud check failure");
        }

        reciverWallet.setBalance(reciverWallet.getBalance().add(request.getAmount()));
        walletRepository.save(reciverWallet);
    }

}
