package com.wallet.safewallet.controller;

import com.wallet.safewallet.dto.ApiResponse;
import com.wallet.safewallet.dto.LoginRequest;
import com.wallet.safewallet.dto.RegisterRequest;
import com.wallet.safewallet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request){
        String token = authService.login(request.getPhone(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.ok("Log in Successful ! ", token));
    }
}
