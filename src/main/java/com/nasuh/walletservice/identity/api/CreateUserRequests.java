package com.nasuh.walletservice.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequests(
    @NotBlank @Email String email,

    @NotBlank String fullName

) {
}
