package com.springboot.service;

import com.springboot.domain.BoardCategory;
import com.springboot.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {

    Page<PostListItemDto> list(BoardCategory category, String q, Pageable pageable);

    PostDetailDto get(Long id, boolean increaseViewCount);

    Long create(String username, PostCreateRequest form, MultipartFile file);  // ★

    void update(Long id, String username, PostUpdateRequest form, MultipartFile file); // ★

    void delete(Long id, String username);

    void addComment(Long postId, String username, CommentCreateRequest form);

    List<PostListItemDto> listNotices(int limit);
}
