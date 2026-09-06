package com.nasuh.walletservice.transfer.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nasuh.walletservice.wallet.domain.CurrencyCode;
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
@Table(name = "transfers")
public class Transfer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "target_wallet_id", nullable = false)
  private Wallet targetWallet;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "source_wallet_id", nullable = false)
  private Wallet sourceWallet;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private CurrencyCode currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TransferStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
  @Column(name = "idempotency_key", unique = true, length = 100)
  private String idempotencyKey;
  @Column(name = "request_hash", length = 64)
  private String requestHash;

  protected Transfer() {

  }

  public Transfer(Wallet sourceWallet, Wallet targetWallet, BigDecimal amount, CurrencyCode currency,
      String idempotencyKey, String requestHash) {

    this.sourceWallet = sourceWallet;
    this.targetWallet = targetWallet;
    this.amount = amount;
    this.currency = currency;
    this.status = TransferStatus.PENDING;
    this.createdAt = LocalDateTime.now();
    this.idempotencyKey = idempotencyKey;
    this.requestHash = requestHash;
  }

  public String getRequestHash() {
    return this.requestHash;
  }

  public void complete() {
    this.status = TransferStatus.COMPLETED;
  }

  public Long getId() {
    return id;
  }

  public Wallet getSourceWallet() {
    return this.sourceWallet;
  }

  public Wallet getTargetWallet() {
    return this.targetWallet;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public CurrencyCode getCurrency() {
    return this.currency;
  }

  public TransferStatus getStatus() {
    return this.status;
  }

  public String getIdemKey() {
    return this.idempotencyKey;
  }

}
