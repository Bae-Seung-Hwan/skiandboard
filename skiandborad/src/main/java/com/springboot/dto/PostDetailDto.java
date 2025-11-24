package com.springboot.dto;

import com.springboot.domain.BoardCategory;

import java.time.Instant;
import java.util.List;

public record PostDetailDto(
	    Long id,
	    String title,
	    String content,
	    String authorName,
	    BoardCategory category,
	    Instant createdAt,
	    Instant updatedAt,
	    long viewCount,
	    boolean hidden,
	    String attachmentUrl,          // 🔥 URL
	    String attachmentOriginalName, // 🔥 원본 파일명
	    Long attachmentSize,           // 🔥 크기
	    List<CommentDto> comments
	) {}
