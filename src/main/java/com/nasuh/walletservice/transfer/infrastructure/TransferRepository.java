package com.nasuh.walletservice.transfer.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasuh.walletservice.transfer.domain.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

}
