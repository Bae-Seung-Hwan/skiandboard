package com.springboot.service;

import com.springboot.domain.BoardCategory;
import com.springboot.dto.*;

import java.util.List;

import org.springframework.data.domain.*;

public interface BoardService {
  Page<PostListItemDto> list(BoardCategory category, String q, Pageable pageable);

  PostDetailDto get(Long id, boolean increaseViewCount);

  Long create(String username, PostCreateRequest form);

  void update(Long id, String username, PostUpdateRequest form);

  void delete(Long id, String username);

  void addComment(Long postId, String username, CommentCreateRequest form);
  List<PostListItemDto> listNotices(int limit);
}
