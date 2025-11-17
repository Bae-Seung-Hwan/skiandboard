package com.springboot.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
    @NotBlank String content
) {}
