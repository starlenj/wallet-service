package com.nasuh.walletservice.identity.api;

import com.nasuh.walletservice.identity.domain.User;

public record UserResponse(
    Long id,
    String email,
    String fullName) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName());
  }
}
