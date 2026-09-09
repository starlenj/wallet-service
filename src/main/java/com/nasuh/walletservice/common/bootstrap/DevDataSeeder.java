package com.nasuh.walletservice.common.bootstrap;

import com.nasuh.walletservice.identity.domain.User;
import com.nasuh.walletservice.identity.infrastructure.UserRepository;
import com.nasuh.walletservice.transfer.application.TransferService;
import com.nasuh.walletservice.transfer.api.CreateTransferRequest;
import com.nasuh.walletservice.wallet.domain.CurrencyCode;
import com.nasuh.walletservice.wallet.domain.Wallet;
import com.nasuh.walletservice.wallet.domain.WalletStatus;
import com.nasuh.walletservice.wallet.infrastructure.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;

  private final TransferService transferService;

  public DevDataSeeder(
      UserRepository userRepository,
      TransferService transferService,
      WalletRepository walletRepository) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
    this.transferService = transferService;
  }

  @Override
  @Transactional
  public void run(String... args) {

    if (userRepository.count() > 0) {
      return;
    }

    User nasuh = new User(
        "nasuh@example.com",
        "Nasuh Test");

    User ali = new User(
        "ali@example.com",
        "Ali Test");

    userRepository.save(nasuh);
    userRepository.save(ali);

    Wallet wallet1 = new Wallet(
        nasuh,
        CurrencyCode.TRY);

    Wallet wallet2 = new Wallet(
        ali,
        CurrencyCode.TRY);

    wallet1.credit(new BigDecimal("10000"));
    wallet2.credit(new BigDecimal("5000"));

    walletRepository.save(wallet1);
    walletRepository.save(wallet2);
    transferService.create(
        "seed-transfer-001",
        new CreateTransferRequest(
            wallet1.getId(),
            wallet2.getId(),
            new BigDecimal("250")));

    transferService.create(
        "seed-transfer-002",
        new CreateTransferRequest(
            wallet2.getId(),
            wallet1.getId(),
            new BigDecimal("75")));
  }
}
