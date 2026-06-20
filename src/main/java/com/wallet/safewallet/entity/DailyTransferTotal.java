package com.wallet.safewallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="daily_transfer_totals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransferTotal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_sent", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSent = BigDecimal.ZERO;

}
