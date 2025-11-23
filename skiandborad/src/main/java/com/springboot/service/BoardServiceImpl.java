package com.springboot.service;

import com.springboot.domain.*;
import com.springboot.dto.*;
import com.springboot.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

  private final BoardPostRepository postRepo;
  private final UserRepository userRepo;
  private final CommentRepository commentRepo;

  // ===== 목록 =====
  @Override
  @Transactional(readOnly = true)
  public Page<PostListItemDto> list(BoardCategory category, String q, Pageable pageable) {
    Page<BoardPost> page;

    boolean hasCategory = (category != null);
    boolean hasQ = (q != null && !q.isBlank());

    if (hasCategory && hasQ) {
      page = postRepo.findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndContentContainingIgnoreCase(
          category, q, category, q, pageable);
    } else if (hasCategory) {
      page = postRepo.findByCategory(category, pageable);
    } else if (hasQ) {
      page = postRepo.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q, pageable);
    } else {
      page = postRepo.findAll(pageable);
    }

    return page.map(this::toListItemDto);
  }

  // ===== 상세 =====
  private String getCurrentUsername() {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
	        return null;
	    }
	    return auth.getName();
	}

	@Override
	@Transactional
	public PostDetailDto get(Long id, boolean increaseViewCount) {

	    var p = postRepo.findByIdWithAuthor(id).orElseThrow();

	    long viewCount = p.getViewCount();
	    if (increaseViewCount) {
	        postRepo.incrementViewCount(id);
	        viewCount++;
	    }

	    String currentUsername = getCurrentUsername();

	    var comments = commentRepo.findByPostOrderByCreatedAtAsc(p).stream()
	        .map(c -> {
	            String authorName = c.getAuthor() != null
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
	                mine   // 🔥 추가된 mine 값
	            );
	        })
	        .toList();

	    return new PostDetailDto(
	        p.getId(),
	        p.getTitle(),
	        p.getContent(),
	        p.getAuthor() != null ? p.getAuthor().getDisplayName() : "(탈퇴회원)",
	        p.getCategory(),
	        p.getCreatedAt(),
	        p.getUpdatedAt(),
	        viewCount,
	        p.isHidden(),
	        comments
	    );
	}

  // ===== 생성 =====
  @Override
  public Long create(String username, PostCreateRequest form) {
    var author = userRepo.findByUsername(username).orElse(null); // 익명/탈퇴 대비
    var post = BoardPost.builder()
        .title(form.title())
        .content(form.content())
        .category(form.category())
        .author(author)
        .viewCount(0)
        .build();
    return postRepo.save(post).getId();
  }

  // ===== 수정 =====
  @Override
  public void update(Long id, String username, PostUpdateRequest form) {
    var post = postRepo.findById(id).orElseThrow();
    var editor = userRepo.findByUsername(username).orElseThrow();

    boolean isOwner = post.getAuthor() != null && post.getAuthor().getId().equals(editor.getId());
    boolean isAdmin = editor.getRole() == Role.ADMIN;
    if (!isOwner && !isAdmin) throw new IllegalStateException("수정 권한이 없습니다.");

    post.setTitle(form.title());
    post.setContent(form.content());
    post.setCategory(form.category());
  }

  // ===== 삭제 =====
  @Override
  public void delete(Long id, String username) {
    var post = postRepo.findById(id).orElseThrow();
    var editor = userRepo.findByUsername(username).orElseThrow();

    boolean isOwner = post.getAuthor() != null && post.getAuthor().getId().equals(editor.getId());
    boolean isAdmin = editor.getRole() == Role.ADMIN;
    if (!isOwner && !isAdmin) throw new IllegalStateException("삭제 권한이 없습니다.");

    postRepo.delete(post);
  }

  // ===== 댓글 작성 =====
  @Override
  public void addComment(Long postId, String username, CommentCreateRequest form) {
    var post = postRepo.findById(postId).orElseThrow();
    var author = (username != null)
        ? userRepo.findByUsername(username).orElse(null)
        : null;

    var comment = Comment.builder()
        .post(post)
        .author(author)
        .content(form.content())
        .build();

    commentRepo.save(comment);
  }
  @Override
  public List<PostListItemDto> listNotices(int limit) {
      Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

      return postRepo
              .findByCategoryAndHiddenFalse(BoardCategory.NOTICE, pageable) // 🔴 여기
              .map(this::toListItemDto)
              .getContent();
  }
  // ===== DTO 매핑 =====
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
