package com.nasuh.walletservice.identity.application;

import org.springframework.stereotype.Service;

import com.nasuh.walletservice.identity.api.CreateUserRequests;
import com.nasuh.walletservice.identity.domain.User;
import com.nasuh.walletservice.identity.infrastructure.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Transactional
  public User create(CreateUserRequests requests) {
    if (userRepository.existsByEmail(requests.email())) {
      throw new IllegalArgumentException("Email already exist");
    }
    User user = new User(
        requests.email(),
        requests.fullName());
    return userRepository.save(user);
  }
}
