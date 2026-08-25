package com.nasuh.walletservice.wallet.api;

import java.math.BigDecimal;

import com.nasuh.walletservice.wallet.domain.Wallet;

public record WalletResponse(
    Long id,
    Long userId,
    String currency,
    BigDecimal balance,
    String status) {
  public static WalletResponse from(Wallet wallet) {
    return new WalletResponse(
        wallet.getId(),
        wallet.getUser().getId(),
        wallet.getCurrency().name(),
        wallet.getBalance(),
        wallet.getStatus().name());
  }
}
