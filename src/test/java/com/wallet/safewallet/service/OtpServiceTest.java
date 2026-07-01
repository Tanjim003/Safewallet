package com.wallet.safewallet.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
 class OtpServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate; //fake redis

    @Mock
    private ValueOperations<String, String> valueOperations; //fake ops

    @InjectMocks
    private OtpService otpService; //real service with mock injected

    void verifyOtpshouldReturnTrueWhenOtpMatches(){

// defining the mock what to return
        String phone = "01712345678";
        String correctOtp = "123456";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:" + phone)).thenReturn(correctOtp);

        // call the real method
        boolean result = otpService.verify(phone, correctOtp);

        // check the result
        assertTrue(result);

        verify(valueOperations).get("otp:" + phone);
        verify(redisTemplate).delete("otp" + phone);
    }

    @Test
    @DisplayName("Should return false when does not match")
    void verifyOtpshouldReturnFalseWhenOtpExpired(){
        String phone = "01712345678";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:" + phone)).thenReturn(null);

        boolean result = otpService.verify(phone, "123456");

        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());

    }

}
