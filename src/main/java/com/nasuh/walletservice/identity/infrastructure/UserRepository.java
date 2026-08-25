package com.nasuh.walletservice.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nasuh.walletservice.identity.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByEmail(String email);
}
