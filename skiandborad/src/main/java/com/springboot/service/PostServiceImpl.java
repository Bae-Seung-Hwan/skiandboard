package com.springboot.service;

import com.springboot.domain.BoardCategory;
import com.springboot.domain.BoardPost;
import com.springboot.dto.*;
import com.springboot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

  private final BoardPostRepository postRepo;
  private final UserRepository userRepo;
  private final CommentRepository commentRepo;

  // ===== 목록 =====
  @Override
  @Transactional(readOnly = true)
  public Page<PostListItemDto> list(Pageable pageable) {
    return postRepo.findAll(pageable).map(this::toListItemDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostListItemDto> listByCategory(BoardCategory category, Pageable pageable) {
    return postRepo.findByCategory(category, pageable).map(this::toListItemDto);
  }

  // (선택) 검색이 있다면 이렇게 사용
  @Transactional(readOnly = true)
  public Page<PostListItemDto> search(String keyword, Pageable pageable) {
    return postRepo
        .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable)
        .map(this::toListItemDto);
  }

  // ===== 생성 =====
  @Override
  public Long create(PostCreateRequest req, Long userId) {
    var author = userRepo.findById(userId).orElse(null); // FK SET NULL 허용
    var post = BoardPost.builder()
        .title(req.title())
        .content(req.content())
        .category(req.category())
        .author(author)
        .viewCount(0)
        .build();
    return postRepo.save(post).getId();
  }

  // ===== 상세 =====
  private String getCurrentUsername() {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
	        return null;
	    }
	    return auth.getName();
	}
  @Override
  @Transactional(readOnly = true)
  public PostDetailDto getDetail(Long id) {
      var p = postRepo.findById(id).orElseThrow();

      String currentUsername = getCurrentUsername();   // 🔹 현재 로그인 사용자

      var comments = commentRepo.findByPostOrderByCreatedAtAsc(p).stream()
          .map(c -> {
              String authorName = (c.getAuthor() != null)
                      ? c.getAuthor().getDisplayName()
                      : "(탈퇴회원)";

              boolean mine = currentUsername != null
                      && c.getAuthor() != null
                      && currentUsername.equals(c.getAuthor().getUsername());

              return new CommentDto(
                  c.getId(),
                  authorName,
                  c.getContent(),
                  c.getCreatedAt(),
                  mine          
              );
          })
          .collect(Collectors.toList());

      return new PostDetailDto(
          p.getId(),
          p.getTitle(),
          p.getContent(),
          p.getAuthor() != null ? p.getAuthor().getDisplayName() : "(탈퇴회원)",
          p.getCategory(),
          p.getCreatedAt(),
          p.getUpdatedAt(),
          p.getViewCount(),
          p.isHidden(),
          comments
      );
  }


  // ===== 조회수 =====
  @Override
  public void increaseViewCount(Long id) {
    postRepo.incrementViewCount(id);
  }

  // ===== 수정/삭제 =====
  @Override
  public void update(Long id, PostUpdateRequest req, Long editorUserId, boolean editorIsAdmin) {
    var p = postRepo.findById(id).orElseThrow();
    var isOwner = p.getAuthor() != null && p.getAuthor().getId().equals(editorUserId);
    if (!isOwner && !editorIsAdmin) throw new IllegalStateException("수정 권한이 없습니다.");
    p.setTitle(req.title());
    p.setContent(req.content());
    p.setCategory(req.category());
  }

  @Override
  public void delete(Long id, Long editorUserId, boolean editorIsAdmin) {
    var p = postRepo.findById(id).orElseThrow();
    var isOwner = p.getAuthor() != null && p.getAuthor().getId().equals(editorUserId);
    if (!isOwner && !editorIsAdmin) throw new IllegalStateException("삭제 권한이 없습니다.");
    postRepo.delete(p);
  }

  // ===== 매핑 헬퍼 =====
  private PostListItemDto toListItemDto(BoardPost p) {
    return new PostListItemDto(
        p.getId(),
        p.getTitle(),
        p.getAuthor() != null ? p.getAuthor().getDisplayName() : "(탈퇴회원)",
        p.getCategory(),
        p.getCreatedAt(),
        p.getViewCount(),
        p.isHidden()
    );
  }
}
