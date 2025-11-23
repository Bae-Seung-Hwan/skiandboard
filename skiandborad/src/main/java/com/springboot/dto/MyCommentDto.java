package com.springboot.dto;

import java.time.Instant;

public record MyCommentDto(
    Long id,
    Long postId,
    String postTitle,
    String content,
    Instant createdAt
) {}
