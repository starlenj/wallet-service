package com.nasuh.walletservice.outbox.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "aggregate_type", nullable = false, length = 100)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private Long aggregateId;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private OutboxStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "published_at", nullable = false)
  private LocalDateTime publishedAt;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Column(name = "last_error")
  private String lastError;

  @Column(name = "processing_started_at")
  private LocalDateTime processingStartedAt;

  @Column(name = "next_attempt_at")
  private LocalDateTime nextAttemptAt;

  protected OutboxEvent() {
  }

  public OutboxEvent(
      String aggregateType,
      Long aggregateId,
      String eventType,
      String payload) {
    this.aggregateId = aggregateId;
    this.aggregateType = aggregateType;
    this.eventType = eventType;
    this.payload = payload;
    this.status = OutboxStatus.PENDING;
    this.createdAt = LocalDateTime.now();
    this.retryCount = 0;
  }

  public Long getId() {
    return this.id;
  }

  public void markPublished() {
    this.status = OutboxStatus.PUBLISHED;
    this.publishedAt = LocalDateTime.now();
    this.processingStartedAt = null;
    this.nextAttemptAt = null;
    this.lastError = null;
  }

  public String getAggregateType() {
    return this.aggregateType;
  }

  public Long getAggregateId() {
    return this.aggregateId;
  }

  public String getPayload() {
    return this.payload;
  }

  public String getEventType() {
    return this.eventType;
  }

  public OutboxStatus getStatus() {
    return this.status;
  }

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public LocalDateTime getPublishedAt() {
    return this.publishedAt;
  }

  public int getRetryCount() {
    return this.retryCount;
  }

  public String getLastError() {
    return this.lastError;
  }

  public void markProccessing() {
    this.status = OutboxStatus.PROCESSING;
    this.processingStartedAt = LocalDateTime.now();
  }

  public void markFailed(String error) {
    this.status = OutboxStatus.FAILED;
    this.retryCount++;
    this.lastError = error;
    this.processingStartedAt = null;
    long delaySeconds = Math.min(60, 5L * retryCount);
    this.nextAttemptAt = LocalDateTime.now().plusSeconds(delaySeconds);
  }

  public void resetForRetry() {
    this.status = OutboxStatus.PENDING;
    this.processingStartedAt = null;
  }

}
