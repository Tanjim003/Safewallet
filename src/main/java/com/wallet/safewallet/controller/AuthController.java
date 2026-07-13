package com.wallet.safewallet.controller;

import com.wallet.safewallet.dto.*;
import com.wallet.safewallet.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@Tag(name = "Authentication", description = "Registration, login, OTP verification")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user and a wallet. Otp sent (printed in console for dev).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or duplicate phone")
    })
    public ResponseEntity<ApiResponseDTO<Void>> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login",
            description = "Authenticates a user with phone and password. Returns a JWT token and user details (phone, fullName, isVerified).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Log in successful return JWT token + user info"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400" , description = "Invalid phone/password, or account not yet verified")
    })
    public ResponseEntity<ApiResponseDTO<LoginResponse>> login(@Valid @RequestBody LoginRequest request){
        LoginResponse loginResponse = authService.login(request.getPhone(), request.getPassword());
        return ResponseEntity.ok(ApiResponseDTO.ok("Log in Successful ! ", loginResponse));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP",
            description = "Verifies the one‑time password sent after registration. If valid, marks the user as verified so they can log in.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP verified successfully - user active now"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid OTP or OTP expired")
    })
    public ResponseEntity<ApiResponseDTO<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request){
        authService.verifyOtp(request.getPhone(), request.getOtp());
        return ResponseEntity.ok(ApiResponseDTO.ok("Phone verified Successfully. You can log in now."));
    }
}
