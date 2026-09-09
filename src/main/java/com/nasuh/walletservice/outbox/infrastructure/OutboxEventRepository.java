package com.nasuh.walletservice.outbox.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.domain.OutboxStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

  @Query(value = """
      SELECT * FROM outbox_events
      Where status = 'PENDING'
      ORDER BY created_at ASC
      LIMIT 100
      FOR UPDATE SKIP LOCKED
      """, nativeQuery = true

  )
  List<OutboxEvent> findPendindForPublishing();

}
