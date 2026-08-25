package com.nasuh.walletservice.ledger.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nasuh.walletservice.transfer.domain.Transfer;
import com.nasuh.walletservice.wallet.domain.Wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transfer_id", nullable = false)
  private Transfer transfer;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LedgerEntryType type;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected LedgerEntry() {
  }

  public LedgerEntry(Wallet wallet, Transfer transfer, LedgerEntryType type, BigDecimal amount) {
    this.wallet = wallet;
    this.transfer = transfer;
    this.type = type;
    this.amount = amount;
    this.createdAt = LocalDateTime.now();

  }
}
