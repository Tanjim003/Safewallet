package com.wallet.safewallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String phone;
    private String fullName;
    private boolean isVerified;

}
