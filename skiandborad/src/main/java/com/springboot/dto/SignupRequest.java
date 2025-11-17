package com.springboot.dto;

import jakarta.validation.constraints.*;

public record SignupRequest(
    @NotBlank @Size(min=3, max=50) String username,
    @NotBlank @Size(min=8, max=64) String password,
    @NotBlank @Size(min=8, max=64) String confirmPassword,
    @NotBlank @Size(min=2, max=100) String displayName
) {}