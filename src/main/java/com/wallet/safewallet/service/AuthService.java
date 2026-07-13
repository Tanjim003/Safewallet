package com.wallet.safewallet.service;

import com.wallet.safewallet.dto.ApiResponseDTO;
import com.wallet.safewallet.dto.LoginResponse;
import com.wallet.safewallet.dto.RegisterRequest;
import com.wallet.safewallet.entity.User;
import com.wallet.safewallet.entity.Wallet;
import com.wallet.safewallet.repository.UserRepository;
import com.wallet.safewallet.repository.WalletRepository;
import com.wallet.safewallet.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    @Transactional
    public ApiResponseDTO<Void> register(RegisterRequest request){
        if(userRepository.existsByPhone(request.getPhone())){
            throw new RuntimeException("Phone number already registered !");
        }
        User user = User.builder()
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isVerified(false)
                .role("USER")
                .build();

        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .userId(savedUser.getId())
                .balance(BigDecimal.ZERO)
                .isFrozen(false)
                .build();

        walletRepository.save(wallet);
        // 4. TODO Week 3: generate and send OTP here
        //otpService.generateAndStore(request.getPhone());
        String otp = otpService.generateOtpandStore(request.getPhone());
        System.out.println("  OTP for " + request.getPhone() + ": " + otp);

        return ApiResponseDTO.ok("Registration successful. Check your phone for OTP.");


    }

    public LoginResponse login(String phone, String password){
        User user = userRepository.findByPhone(phone)
                .orElseThrow(()-> new RuntimeException("Invalid phone or password"));

        if(!user.getIsVerified()){
            throw new RuntimeException("Account not verified. Please verify OTP first ! ");
        }

        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Invalid phone or password ! ");
        }

        String token =  jwtUtil.generateToken(user.getPhone());

        return new LoginResponse(token,
                user.getPhone(),
                user.getFullName(),
                user.getIsVerified());


    }

    public void verifyOtp(String phone, String otp){
        if(!otpService.verify(phone, otp)){
            throw new RuntimeException("Invalid or expired otp ! ");
        }
        User user = userRepository.findByPhone(phone)
                .orElseThrow(()-> new RuntimeException("User not found !"));
        user.setIsVerified(true);
        userRepository.save(user);
    }


}
