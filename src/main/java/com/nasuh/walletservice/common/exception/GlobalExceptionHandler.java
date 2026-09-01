package com.nasuh.walletservice.common.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
      ResourceNotFoundException exception,
      HttpServletRequest request) {
    ApiErrorResponse response = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.NOT_FOUND.value(),
        HttpStatus.NOT_FOUND.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(BadRequestResponse.class)
  public ResponseEntity<ApiErrorResponse> handleResourceBadRequst(
      ResourceNotFoundException exception,
      HttpServletRequest request) {
    ApiErrorResponse response = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(InsufficientBalanceError.class)
  public ResponseEntity<ApiErrorResponse> handleResourceBalance(
      InsufficientBalanceError exception,
      HttpServletRequest request) {
    ApiErrorResponse response = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        HttpStatus.BAD_REQUEST.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConflictResponse.class)
  public ResponseEntity<ApiErrorResponse> conflictCheckResponse(
      ConflictResponse exception,
      HttpServletRequest request) {
    ApiErrorResponse response = new ApiErrorResponse(
        Instant.now(),
        HttpStatus.CONFLICT.value(),
        HttpStatus.CONFLICT.getReasonPhrase(),
        exception.getMessage(),
        request.getRequestURI());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }
}
