package com.wallet.safewallet.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    public final Counter transferCounter;
    public final Counter fraudBlockCounter;
    public final Counter fraudFlagCounter;
    public final Timer transferTimer;

    public MetricsService(MeterRegistry registry){
        transferCounter = Counter.builder("wallet.transfers")
                .description("Total number of transfer")
                .register(registry);
        fraudBlockCounter = Counter.builder("wallet.fruad.blocks")
                .description("Transfer blocked by fruad engine")
                .register(registry);
        fraudFlagCounter = Counter.builder("wallet.fraud.flags")
                .description("Transfers flagged by fraud engine")
                .register(registry);
        transferTimer = Timer.builder("wallet.transfers.latency")
                .description("Transfer execution time")
                .register(registry);
    }

}
