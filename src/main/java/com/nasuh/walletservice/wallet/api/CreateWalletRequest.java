package com.nasuh.walletservice.wallet.api;

import com.nasuh.walletservice.wallet.domain.CurrencyCode;

import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
    @NotNull Long userId,
    @NotNull CurrencyCode currency) {
}
