package com.nasuh.walletservice.common.bootstrap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nasuh.walletservice.outbox.application.OutboxRecoveryService;

@Component
public class OutboxRecoveryScheduler {
  private final OutboxRecoveryService outboxRecoveryService;

  public OutboxRecoveryScheduler(OutboxRecoveryService outboxRecoveryService) {
    this.outboxRecoveryService = outboxRecoveryService;
  }

  @Scheduled(fixedDelay = 5000)
  public void recoveryFailedEvents() {
    outboxRecoveryService.recoveryFailedEvents();
  }

  @Scheduled(fixedDelay = 30000)
  public void recoveryStuckEvents() {
    outboxRecoveryService.recoveryStuckProcssingEvents();
  }
}
