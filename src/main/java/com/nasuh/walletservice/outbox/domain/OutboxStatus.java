package com.nasuh.walletservice.outbox.domain;

public enum OutboxStatus {
  PENDING,
  PROCESSING,
  FAILED,
  PUBLISHED
}
