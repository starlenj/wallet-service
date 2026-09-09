package com.nasuh.walletservice.outbox.application;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.infrastructure.OutboxEventRepository;

import jakarta.transaction.Transactional;

@Service
public class OutboxStatusService {

  private final OutboxEventRepository outboxEventRepository;

  public OutboxStatusService(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Transactional
  public void markPublished(Long eventId) {
    OutboxEvent event = outboxEventRepository.findById(eventId).orElseThrow();
    event.markPublished();
  }

  @Transactional
  public void markFailed(Long eventId, String error) {
    OutboxEvent event = outboxEventRepository.findById(eventId).orElseThrow();
    event.markFailed(error);
  }
}
