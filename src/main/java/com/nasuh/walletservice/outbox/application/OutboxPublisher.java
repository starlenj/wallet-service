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
  private final KafkaTemplate<String, String> kafkaTemplate;

  public OutboxPublisher(
      OutboxEventRepository outboxEventRepository,
      KafkaTemplate<String, String> kafkaTemplate) {
    this.outboxEventRepository = outboxEventRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEvent> events = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    for (OutboxEvent event : events) {
      publish(event);
    }
  }

  private void publish(OutboxEvent event) {
    kafkaTemplate.send(KafkaConfig.TRANSFER_COMPLETED_TOPIC, event.getAggregateId().toString(), event.getPayload())
        .join();
    event.markPublished();
  }

}
