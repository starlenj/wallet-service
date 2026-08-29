package com.nasuh.walletservice.transfer.api;

import java.math.BigDecimal;

import com.nasuh.walletservice.transfer.domain.Transfer;

public record TransferResponse(
    Long id,
    Long sourceWalletId,
    Long targetWalletId,
    BigDecimal amount,
    String currency,
    String status) {

  public static TransferResponse from(Transfer transfer) {
    return new TransferResponse(
        transfer.getId(),
        transfer.getSourceWallet().getId(),
        transfer.getTargetWallet().getId(),
        transfer.getAmount(),
        transfer.getCurrency().name(),
        transfer.getStatus().name());
  }

}
