package com.nasuh.walletservice.transfer.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nasuh.walletservice.transfer.application.TransferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {
  private final TransferService transferService;

  public TransferController(TransferService transferService) {
    this.transferService = transferService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TransferResponse create(@Valid @RequestBody CreateTransferRequest request) {
    return TransferResponse.from(transferService.create(request));
  }

  @GetMapping("/{id}")
  public TransferResponse getById(@PathVariable Long id) {
    return TransferResponse.from(transferService.findById(id));
  }
}
