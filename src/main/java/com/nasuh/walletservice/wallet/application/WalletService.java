package com.nasuh.walletservice.wallet.application;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.identity.domain.User;
import com.nasuh.walletservice.identity.infrastructure.UserRepository;
import com.nasuh.walletservice.wallet.api.CreateWalletRequest;
import com.nasuh.walletservice.wallet.domain.Wallet;
import com.nasuh.walletservice.wallet.infrastructure.WalletRepository;

import jakarta.transaction.Transactional;

@Service
public class WalletService {

  private final WalletRepository walletRepository;
  private final UserRepository userRepository;

  public WalletService(
      WalletRepository walletRepository,
      UserRepository userRepository) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
  }

  @Transactional
  public Wallet create(
      CreateWalletRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new IllegalArgumentException("User Not Found"));
    Wallet wallet = new Wallet(user, request.currency());
    return walletRepository.save(wallet);
  }

  @Transactional()
  public Wallet findById(Long id) {
    return walletRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("wallet not found"));
  }

  @Transactional
  public Wallet deposit(Long walletId, BigDecimal amount) {
    Wallet wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new IllegalArgumentException("Wallet not found" + walletId));
    wallet.credit(amount);
    return wallet;
  }
}
