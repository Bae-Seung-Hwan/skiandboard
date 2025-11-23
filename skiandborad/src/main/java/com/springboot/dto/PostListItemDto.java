package com.springboot.dto;

import com.springboot.domain.BoardCategory;
import java.time.Instant;

public record PostListItemDto(
  Long id, String title, String authorName,
  BoardCategory category, Instant createdAt, long viewCount, boolean hidden
) {}
