package com.wallet.safewallet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.
                csrf(csrf-> csrf.disable())
                .authorizeHttpRequests(auth-> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

}
