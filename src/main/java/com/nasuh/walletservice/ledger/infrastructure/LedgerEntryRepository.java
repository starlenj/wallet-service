package com.nasuh.walletservice.ledger.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasuh.walletservice.ledger.domain.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

}
