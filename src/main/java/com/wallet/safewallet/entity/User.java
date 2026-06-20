package com.wallet.safewallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="phone", nullable = false, unique = true,length = 15)
    private String phone;

    @Column(name="full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="is_verified")
    private Boolean isVerified = false;

    @Column(name="role", nullable = false, length = 20)
    private String role = "USER";

    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }



}
