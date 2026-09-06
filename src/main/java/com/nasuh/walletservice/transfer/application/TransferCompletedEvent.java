package com.nasuh.walletservice.transfer.application;

import java.math.BigDecimal;

public record TransferCompletedEvent(
    Long transferId,
    Long sourceWalletId,
    Long targetWalletId,
    BigDecimal amount,
    String currency) {
}
