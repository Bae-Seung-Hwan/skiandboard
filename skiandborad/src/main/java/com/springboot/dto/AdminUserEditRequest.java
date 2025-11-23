package com.springboot.dto;

import com.springboot.domain.Role;

public record AdminUserEditRequest(
    String username,     // 읽기 전용 (폼에서 disabled)
    String displayName,
    Role role,
    boolean enabled
) {}
