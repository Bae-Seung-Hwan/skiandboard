package com.springboot.dto;

import java.time.Instant;

public record CommentDto(
  Long id, String authorName, String content, Instant createdAt, boolean mine
) {}
