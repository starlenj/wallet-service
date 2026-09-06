package com.nasuh.walletservice.outbox.application;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.infrastructure.OutboxEventRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxService {
  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  public OutboxService(
      OutboxEventRepository outboxEventRepository,
      ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.outboxEventRepository = outboxEventRepository;
  }

  public void save(String aggregateType, Long aggregateId, String eventType, Object event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      OutboxEvent outboxEvent = new OutboxEvent(aggregateType, aggregateId, eventType, payload);
      outboxEventRepository.save(outboxEvent);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize outbox event", e);
    }
  }
}
