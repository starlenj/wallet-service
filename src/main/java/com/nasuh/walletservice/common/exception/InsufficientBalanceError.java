package com.nasuh.walletservice.common.exception;

public class InsufficientBalanceError extends RuntimeException {
  public InsufficientBalanceError(String message) {
    super(message);
  }
}
