package com.wallet.safewallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Phone is required !")
    @Pattern(regexp = "^01[3-9]\\d{8}$", message = "Invalid BD phone number")
    private String phone;

    @NotBlank(message = "Full name is required !")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String fullName;

    @NotBlank(message = "Password is required !")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

}
