package com.nasuh.walletservice.wallet.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasuh.walletservice.wallet.domain.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
