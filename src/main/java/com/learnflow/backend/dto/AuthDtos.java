package com.learnflow.backend.dto;

import com.learnflow.backend.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {}

    public record SignInRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}

    public record SignUpRequest(
        @NotBlank @Size(min = 3, max = 120) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 120) String password
    ) {}

    public record SessionUser(
        Long id,
        String fullName,
        String email,
        Role role
    ) {}

    public record AuthResponse(
        String token,
        SessionUser user
    ) {}
}
