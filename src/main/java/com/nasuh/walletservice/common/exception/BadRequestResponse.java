package com.nasuh.walletservice.common.exception;

public class BadRequestResponse extends RuntimeException {
  public BadRequestResponse(String message) {
    super(message);
  }
}
