package com.springboot.service;

import com.springboot.domain.BoardCategory;
import com.springboot.dto.PostCreateRequest;
import com.springboot.dto.PostDetailDto;
import com.springboot.dto.PostListItemDto;
import com.springboot.dto.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
  Page<PostListItemDto> list(Pageable pageable);
  Page<PostListItemDto> listByCategory(BoardCategory category, Pageable pageable);
  Long create(PostCreateRequest req, Long userId);
  PostDetailDto getDetail(Long id);       // 조회수 증가 포함 X (분리)
  void increaseViewCount(Long id);        // 조회수 증가
  void update(Long id, PostUpdateRequest req, Long editorUserId, boolean editorIsAdmin);
  void delete(Long id, Long editorUserId, boolean editorIsAdmin);
}
