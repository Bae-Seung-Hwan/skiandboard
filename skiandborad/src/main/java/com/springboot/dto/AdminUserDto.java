package com.springboot.dto;

import com.springboot.domain.Role;

public record AdminUserDto(
    Long id,
    String username,
    String displayName,
    Role role,
    boolean enabled
) {}
