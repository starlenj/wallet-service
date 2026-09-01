package com.nasuh.walletservice.transfer.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasuh.walletservice.transfer.domain.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

  Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

  @Query(value = """
      SELECT pg_advisory_xact_lock(hashtextextended(:idempotencyKey,0))
      """, nativeQuery = true)
  void lockIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
}
