package com.springboot.service;

import com.springboot.domain.BoardPost;
import com.springboot.domain.Comment;
import com.springboot.dto.*;
import com.springboot.repository.BoardPostRepository;
import com.springboot.repository.CommentRepository;
import com.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

  private final UserRepository userRepository;
  private final BoardPostRepository postRepository;
  private final CommentRepository commentRepository;

  @Override
  public List<PostListItemDto> getRecentPosts(String username) {
    return postRepository.findTop5ByAuthor_UsernameOrderByCreatedAtDesc(username)
        .stream()
        .map(this::toPostListItemDto)
        .toList();
  }

  @Override
  public List<CommentDto> getRecentComments(String username) {
    return commentRepository.findTop5ByAuthor_UsernameOrderByCreatedAtDesc(username)
        .stream()
        .map(this::toCommentDto)
        .toList();
  }

  @Override
  public List<PostListItemDto> getAllPosts(String username) {
    return postRepository.findByAuthor_UsernameOrderByCreatedAtDesc(username)
        .stream()
        .map(this::toPostListItemDto)
        .toList();
  }

  @Override
  public List<MyCommentDto> getAllComments(String username) {
    return commentRepository.findByAuthor_UsernameOrderByCreatedAtDesc(username)
        .stream()
        .map(c -> new MyCommentDto(
            c.getId(),
            c.getPost().getId(),
            c.getPost().getTitle(),
            c.getContent(),
            c.getCreatedAt()
        ))
        .toList();
  }

  @Override
  public ProfileUpdateRequest getProfile(String username) {
    var user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    return new ProfileUpdateRequest(user.getDisplayName());
  }

  @Override
  @Transactional
  public void updateProfile(String username, ProfileUpdateRequest req) {
    var user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    user.setDisplayName(req.displayName());
  }

  // ===== 매핑 헬퍼 =====

  private PostListItemDto toPostListItemDto(BoardPost p) {
    String authorName = p.getAuthor() != null
        ? p.getAuthor().getDisplayName()
        : "(탈퇴회원)";
    return new PostListItemDto(
        p.getId(),
        p.getTitle(),
        authorName,
        p.getCategory(),
        p.getCreatedAt(),
        p.getViewCount(),
        p.isHidden()
    );
  }

  private CommentDto toCommentDto(Comment c) {
    String authorName = c.getAuthor() != null
        ? c.getAuthor().getDisplayName()
        : "(탈퇴회원)";
    return new CommentDto(
        c.getId(),
        authorName,
        c.getContent(),
        c.getCreatedAt(),
        true
    );
  }
}

