package com.nasuh.walletservice.transfer.application;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.common.exception.ResourceNotFoundException;
import com.nasuh.walletservice.ledger.domain.LedgerEntry;
import com.nasuh.walletservice.ledger.domain.LedgerEntryType;
import com.nasuh.walletservice.ledger.infrastructure.LedgerEntryRepository;
import com.nasuh.walletservice.transfer.api.CreateTransferRequest;
import com.nasuh.walletservice.transfer.domain.Transfer;
import com.nasuh.walletservice.transfer.infrastructure.TransferRepository;
import com.nasuh.walletservice.wallet.domain.Wallet;
import com.nasuh.walletservice.wallet.infrastructure.WalletRepository;

import jakarta.transaction.Transactional;

@Service
public class TransferService {
  private final WalletRepository walletRepository;
  private final TransferRepository transferRepository;
  private final LedgerEntryRepository ledgerEntryRepository;

  public TransferService(
      WalletRepository walletRepository,
      TransferRepository transferRepository,
      LedgerEntryRepository ledgerEntryRepository) {
    this.ledgerEntryRepository = ledgerEntryRepository;
    this.transferRepository = transferRepository;
    this.walletRepository = walletRepository;
  }

  @Transactional
  public Transfer create(String idemmpotencyKey, CreateTransferRequest request) {

    if (request.sourceWalletId().equals(request.targetWalletId())) {
      throw new IllegalArgumentException("Source and target wallets cannot be the same");
    }
    transferRepository.lockIdempotencyKey(idemmpotencyKey);

    Transfer existingTransfer = transferRepository.findByIdempotencyKey(idemmpotencyKey).orElse(null);

    if (existingTransfer != null) {
      return existingTransfer;
    }

    Long firstWalletId = Math.min(
        request.sourceWalletId(),
        request.targetWalletId());

    Long secondWalletId = Math.max(
        request.sourceWalletId(),
        request.targetWalletId());

    Wallet sourceWallet = walletRepository.findBydIdForUpdate(firstWalletId)
        .orElseThrow(() -> new IllegalArgumentException("Source wallet not found"));

    Wallet targetWallet = walletRepository.findBydIdForUpdate(secondWalletId)
        .orElseThrow(() -> new IllegalArgumentException("Target wallet not found"));
    if (sourceWallet.getCurrency() != targetWallet.getCurrency()) {
      throw new IllegalArgumentException("Wallet currency must match");
    }
    sourceWallet.debit(request.amount());
    targetWallet.credit(request.amount());

    Transfer transfer = new Transfer(
        sourceWallet,
        targetWallet,
        request.amount(),
        sourceWallet.getCurrency(),
        idemmpotencyKey);

    transferRepository.save(transfer);
    LedgerEntry debitEntry = new LedgerEntry(sourceWallet, transfer, LedgerEntryType.DEBIT, request.amount());
    LedgerEntry creditEntry = new LedgerEntry(targetWallet, transfer, LedgerEntryType.CREDIT, request.amount());

    ledgerEntryRepository.save(debitEntry);
    ledgerEntryRepository.save(creditEntry);
    transfer.complete();
    return transfer;
  }

  @Transactional
  public Transfer findById(Long id) {
    return transferRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Transfer not found " + id));
  }
}
