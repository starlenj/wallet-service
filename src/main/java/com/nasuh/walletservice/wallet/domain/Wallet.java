package com.nasuh.walletservice.wallet.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.nasuh.walletservice.identity.domain.User;

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
@Table(name = "wallets")
public class Wallet {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 3)
  private CurrencyCode currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private WalletStatus status;

  @Column(nullable = false, precision = 19, scale = 4)
  private BigDecimal balance;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  protected Wallet() {

  }

  public Wallet(User user, CurrencyCode currency) {
    this.user = user;
    this.currency = currency;
    this.balance = BigDecimal.ZERO;
    this.status = WalletStatus.ACTIVE;
    this.createdAt = LocalDateTime.now();

  }

  public Long getId() {
    return id;
  }

  public WalletStatus getStatus() {
    return status;
  }

  public User getUser() {
    return user;
  }

  public CurrencyCode getCurrency() {
    return currency;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void credit(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
    this.balance = this.balance.add(amount);
  }

  public void debit(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
    if (this.balance.compareTo(amount) < 0) {
      throw new IllegalArgumentException("Insufficient balance");
    }
    this.balance = this.balance.subtract(amount);
  }

}
