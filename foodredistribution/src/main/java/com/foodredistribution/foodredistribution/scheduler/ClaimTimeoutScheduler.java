package com.foodredistribution.foodredistribution.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foodredistribution.foodredistribution.service.ClaimLifecycleService;

@Component
public class ClaimTimeoutScheduler {

    private final ClaimLifecycleService claimLifecycleService;

    public ClaimTimeoutScheduler(ClaimLifecycleService claimLifecycleService) {
        this.claimLifecycleService = claimLifecycleService;
    }

    // Run every hour
    @Scheduled(fixedRate = 3600000)
    public void processStaleClaims() {
        claimLifecycleService.timeoutStaleClaims();
    }
}
