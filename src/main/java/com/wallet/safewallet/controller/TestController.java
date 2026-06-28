package com.wallet.safewallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    @GetMapping("test/protected")
    public ResponseEntity<String> protectedEndpoint(){
        return ResponseEntity.ok("You are authorized");
    }

}
