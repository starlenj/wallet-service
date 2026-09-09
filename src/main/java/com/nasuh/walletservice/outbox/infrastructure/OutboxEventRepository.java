package com.nasuh.walletservice.outbox.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.domain.OutboxStatus;

public interface OutboxEventRepository
    extends JpaRepository<OutboxEvent, Long> {

  List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(
      OutboxStatus status);

  @Query(value = """
      SELECT *
      FROM outbox_events
      WHERE status = 'PENDING'
      ORDER BY created_at ASC
      LIMIT 100
      FOR UPDATE SKIP LOCKED
      """, nativeQuery = true)
  List<OutboxEvent> findPendingForPublishing();

  @Query("""
      SELECT e
      FROM OutboxEvent e
      WHERE e.status = :status
        AND e.retryCount < :maxRetries
        AND e.nextAttemptAt <= :now
      """)
  List<OutboxEvent> findRetryableEvents(
      @Param("status") OutboxStatus status,
      @Param("maxRetries") int maxRetries,
      @Param("now") LocalDateTime now);

  @Query("""
      SELECT e
      FROM OutboxEvent e
      WHERE e.status = :status
        AND e.processingStartedAt < :threshold
      """)
  List<OutboxEvent> findStuckProcessingEvents(
      @Param("status") OutboxStatus status,
      @Param("threshold") LocalDateTime threshold);
}
