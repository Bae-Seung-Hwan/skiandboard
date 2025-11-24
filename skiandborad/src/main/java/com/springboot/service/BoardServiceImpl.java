package com.springboot.service;

import com.springboot.domain.*;
import com.springboot.dto.*;
import com.springboot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardServiceImpl implements BoardService {

    private final BoardPostRepository postRepo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;

    @Value("${app.upload-dir}")
    private String uploadDir;

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

    // ===== 현재 로그인 유저명 조회 =====
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return null;
        }
        return auth.getName();
    }

    // ===== 상세 =====
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
                            mine
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
                p.getAttachmentUrl(),          // ★ URL
                p.getAttachmentOriginalName(), // ★ 원본 파일명
                p.getAttachmentSize(),         // ★ 크기
                comments
        );
    }

    // ===== 파일 저장 헬퍼 =====
    private record FileInfo(String url, String originalName, Long size) {}

    private FileInfo saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        try {
            String originalName = file.getOriginalFilename();
            long size = file.getSize();

            String ext = "";
            if (originalName != null && originalName.lastIndexOf('.') != -1) {
                ext = originalName.substring(originalName.lastIndexOf('.'));
            }

            String savedName = UUID.randomUUID() + ext;

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path target = uploadPath.resolve(savedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = "/skiandboard/uploads/" + savedName;   // WebMvcConfig와 매칭

            return new FileInfo(url, originalName, size);
        } catch (Exception e) {
            e.printStackTrace(); // 필요하면 logger로 변경
            return null;
        }
    }

    // ===== 생성 =====
    @Override
    public Long create(String username, PostCreateRequest form, MultipartFile file) {

        var author = userRepo.findByUsername(username).orElse(null);

        FileInfo info = saveFile(file); // ★ 파일 저장

        var post = BoardPost.builder()
                .title(form.title())
                .content(form.content())
                .category(form.category())
                .author(author)
                .viewCount(0)
                .hidden(false)
                .attachmentUrl(info != null ? info.url() : null)
                .attachmentOriginalName(info != null ? info.originalName() : null)
                .attachmentSize(info != null ? info.size() : null)
                .build();

        return postRepo.save(post).getId();
    }

    // ===== 수정 =====
    @Override
    public void update(Long id, String username, PostUpdateRequest form, MultipartFile file) {
        var post = postRepo.findById(id).orElseThrow();
        var editor = userRepo.findByUsername(username).orElseThrow();

        boolean isOwner = post.getAuthor() != null && post.getAuthor().getId().equals(editor.getId());
        boolean isAdmin = editor.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) throw new IllegalStateException("수정 권한이 없습니다.");

        post.setTitle(form.title());
        post.setContent(form.content());
        post.setCategory(form.category());

        // 새 파일이 올라온 경우에만 교체
        FileInfo info = saveFile(file);
        if (info != null) {
            post.setAttachmentUrl(info.url());
            post.setAttachmentOriginalName(info.originalName());
            post.setAttachmentSize(info.size());
        }
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
                .findByCategoryAndHiddenFalse(BoardCategory.NOTICE, pageable)
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
