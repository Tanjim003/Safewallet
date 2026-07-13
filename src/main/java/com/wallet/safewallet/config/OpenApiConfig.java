package com.wallet.safewallet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI safeWalletOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeWallet API")
                        .description("Secure digital wallet backend – registration, OTP, JWT auth, transfers, top‑up, withdrawal, transaction history, fraud engine.")
                        .version("1.0.0"));
    }
}
