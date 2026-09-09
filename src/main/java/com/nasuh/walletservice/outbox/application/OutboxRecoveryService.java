package com.nasuh.walletservice.outbox.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.domain.OutboxStatus;
import com.nasuh.walletservice.outbox.infrastructure.OutboxEventRepository;

import jakarta.transaction.Transactional;

@Service
public class OutboxRecoveryService {
  private static final int MAX_RETIRES = 5;

  private final OutboxEventRepository outboxEventRepository;

  public OutboxRecoveryService(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Transactional
  public void recoveryFailedEvents() {

    List<OutboxEvent> events = outboxEventRepository.findRetryableEvents(OutboxStatus.FAILED, MAX_RETIRES,
        LocalDateTime.now());

    for (OutboxEvent event : events) {
      event.resetForRetry();
    }
  }

  @Transactional
  public void recoveryStuckProcssingEvents() {
    LocalDateTime threshold = LocalDateTime.now().minusMinutes(2);

    List<OutboxEvent> events = outboxEventRepository.findStuckProcessingEvents(OutboxStatus.PROCESSING, threshold);

    for (OutboxEvent event : events) {
      event.resetForRetry();
    }
  }
}
