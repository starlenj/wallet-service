package com.nasuh.walletservice.outbox.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.infrastructure.OutboxEventRepository;

import jakarta.transaction.Transactional;

@Service
public class OutboxClaimService {
  private final OutboxEventRepository outboxEventRepository;

  public OutboxClaimService(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Transactional
  public List<Long> claimPendinEvents() {
    List<OutboxEvent> events = outboxEventRepository.findPendindForPublishing();

    for (OutboxEvent event : events) {
      event.markProccessing();
    }
    return events.stream().map(OutboxEvent::getId).toList();
  }

}
