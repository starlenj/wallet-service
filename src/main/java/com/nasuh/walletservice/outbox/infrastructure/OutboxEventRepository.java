package com.nasuh.walletservice.outbox.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasuh.walletservice.outbox.domain.OutboxEvent;
import com.nasuh.walletservice.outbox.domain.OutboxStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

}
