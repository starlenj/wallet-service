package com.nasuh.walletservice.outbox.application;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nasuh.walletservice.common.config.KafkaConfig;
import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.domain.OutboxStatus;
import com.nasuh.walletservice.outbox.infrastructure.OutboxEventRepository;

import jakarta.transaction.Transactional;

@Service
public class OutboxPublisher {
  private final OutboxEventRepository outboxEventRepository;
  private final OutboxClaimService outboxClaimService;
  private final OutboxStatusService outboxStatusService;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public OutboxPublisher(
      OutboxEventRepository outboxEventRepository,
      OutboxClaimService outboxClaimService,
      OutboxStatusService outboxStatusService,
      KafkaTemplate<String, String> kafkaTemplate) {
    this.outboxEventRepository = outboxEventRepository;
    this.kafkaTemplate = kafkaTemplate;
    this.outboxClaimService = outboxClaimService;
    this.outboxStatusService = outboxStatusService;
  }

  @Scheduled(fixedDelay = 1000)
  public void publishPendingEvents() {
    List<Long> eventIds = outboxClaimService.claimPendinEvents();
    for (Long eventId : eventIds) {
      publish(eventId);
    }
  }

  private void publish(Long eventId) {
    OutboxEvent event = outboxEventRepository.findById(eventId).orElseThrow();
    try {
      kafkaTemplate.send(KafkaConfig.TRANSFER_COMPLETED_TOPIC, event.getId().toString(), event.getPayload())
          .join();
      outboxStatusService.markPublished(eventId);

    } catch (Exception e) {
      outboxStatusService.markFailed(eventId, e.getMessage());
    }

  }

}
