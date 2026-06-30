package com.wallet.safewallet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final StringRedisTemplate redisTemplate;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_MINUTES = 5;

    //generate a 6 digit random otp
    public String generateOtpandStore(String phone){
        String otp = String.valueOf(100000 + new SecureRandom().nextInt(900000));
        redisTemplate.opsForValue().set("otp:" + phone, otp, 5, TimeUnit.MINUTES);
        return otp; // log it for now , send via sms later

    }

    public boolean verify(String phone, String otp){
        String stored = redisTemplate.opsForValue().get("otp:" + phone);
        if(stored != null && stored.equals(otp)){
            redisTemplate.delete("otp:" + phone);
            return true;
        }
        return false;
    }


}
