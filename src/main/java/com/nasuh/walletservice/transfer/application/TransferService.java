package com.nasuh.walletservice.transfer.application;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.common.exception.BadRequestResponse;
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
  public Transfer create(CreateTransferRequest request) {
    if (request.sourceWalletId().equals(request.targetWalletId())) {
      throw new BadRequestResponse("Source and target wallets cannot be the same");
    }
    Wallet sourceWallet = walletRepository.findById(request.sourceWalletId())
        .orElseThrow(() -> new ResourceNotFoundException("Source wallet not found"));

    Wallet targetWallet = walletRepository.findById(request.targetWalletId())
        .orElseThrow(() -> new ResourceNotFoundException("Target wallet not found"));
    if (sourceWallet.getCurrency() != targetWallet.getCurrency()) {
      throw new BadRequestResponse("Wallet currency must match");
    }
    sourceWallet.debit(request.amount());
    targetWallet.credit(request.amount());

    Transfer transfer = new Transfer(
        sourceWallet,
        targetWallet,
        request.amount(),
        sourceWallet.getCurrency());

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
    return transferRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transfer Not found"));
  }
}
