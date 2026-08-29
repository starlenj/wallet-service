package com.nasuh.walletservice.transfer.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateTransferRequest(
    @NotNull Long sourceWalletId,
    @NotNull Long targetWalletId,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
}
