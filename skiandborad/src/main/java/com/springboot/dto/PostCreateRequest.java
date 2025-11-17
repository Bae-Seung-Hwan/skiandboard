package com.springboot.dto;

import com.springboot.domain.BoardCategory;
import jakarta.validation.constraints.*;

public record PostCreateRequest(
  @NotBlank @Size(max=120) String title,
  @NotBlank String content,
  @NotNull BoardCategory category
) {}
