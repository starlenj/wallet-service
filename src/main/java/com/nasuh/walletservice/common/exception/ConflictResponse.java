package com.nasuh.walletservice.common.exception;

public class ConflictResponse extends RuntimeException {
  public ConflictResponse(String message) {
    super(message);
  }
}
