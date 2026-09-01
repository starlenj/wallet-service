package com.nasuh.walletservice.wallet.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nasuh.walletservice.wallet.domain.Wallet;

import jakarta.persistence.LockModeType;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      select w from Wallet w where w.id = :id
      """)
  Optional<Wallet> findBydIdForUpdate(@Param("id") Long id);
}
