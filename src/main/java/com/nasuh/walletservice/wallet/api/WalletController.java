package com.nasuh.walletservice.wallet.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nasuh.walletservice.wallet.application.WalletService;
import com.nasuh.walletservice.wallet.domain.Wallet;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WalletResponse create(@Valid @RequestBody CreateWalletRequest request) {
    return WalletResponse.from(walletService.create(request));
  }

  @GetMapping("/{id}")
  public WalletResponse getById(@PathVariable Long id) {
    return WalletResponse.from(walletService.findById(id));
  }

  @PostMapping("{id}/deposit")
  public WalletResponse deposit(@PathVariable Long id, @Valid @RequestBody DepositRequest request) {
    Wallet wallet = walletService.deposit(id, request.amount());
    return WalletResponse.from(wallet);
  }
}
