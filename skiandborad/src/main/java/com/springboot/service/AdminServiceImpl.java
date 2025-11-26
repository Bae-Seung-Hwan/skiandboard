package com.springboot.service;

import com.springboot.domain.User;
import com.springboot.dto.AdminUserDto;
import com.springboot.dto.AdminUserEditRequest;
import com.springboot.dto.PostListItemDto;
import com.springboot.dto.ResortDto;
import com.springboot.repository.BoardPostRepository;
import com.springboot.repository.CommentRepository;
import com.springboot.repository.SkiResortRepository;
import com.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SkiResortRepository skiResortRepository;
    private final BoardPostRepository boardPostRepository;
    private final CommentRepository commentRepository;

    // ================= 대시보드 요약 =================

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public long countResorts() {
        return skiResortRepository.count();
    }

    @Override
    public long countPosts() {
        return boardPostRepository.count();
    }

    @Override
    public long countComments() {
        return commentRepository.count();
    }

    @Override
    public List<AdminUserDto> getRecentUsers(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return userRepository.findAll(pageable)
                .map(this::toAdminUserDto)
                .getContent();
    }

    @Override
    public List<PostListItemDto> getRecentPosts(int limit) {
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return boardPostRepository.findAll(pageable)
                .map(p -> new PostListItemDto(
                        p.getId(),
                        p.getTitle(),
                        p.getAuthor() != null ? p.getAuthor().getDisplayName() : "(탈퇴회원)",
                        p.getCategory(),
                        p.getCreatedAt(),
                        p.getViewCount(),
                        p.isHidden()
                ))
                .getContent();
    }

    // 목록 페이지용

    @Override
    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toAdminUserDto)
                .toList();
    }

    @Override
    public List<ResortDto> getAllResorts() {
        return skiResortRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(ResortDto::from)
                .toList();
    }

    // 회원 관리 CRUD

    @Override
    public AdminUserEditRequest getUserForEdit(Long userId) {
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));
        return new AdminUserEditRequest(
                u.getUsername(),
                u.getDisplayName(),
                u.getRole(),
                u.isEnabled()
        );
    }

    @Override
    @Transactional
    public void updateUser(Long userId, AdminUserEditRequest req) {
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        u.setDisplayName(req.displayName());
        u.setRole(req.role());
        u.setEnabled(req.enabled());
    }

    @Override
    @Transactional
    public void toggleUserEnabled(Long userId) {
        var u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));
        u.setEnabled(!u.isEnabled());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return;
        }
        
        userRepository.deleteById(userId);
    }

    // 게시글 관리

    @Override
    public List<PostListItemDto> getAllPostsForAdmin() {
        return boardPostRepository.findAllByOrderByIdDesc()
                .stream()
                .map(p -> new PostListItemDto(
                        p.getId(),
                        p.getTitle(),
                        p.getAuthor() != null ? p.getAuthor().getDisplayName() : "(탈퇴회원)",
                        p.getCategory(),
                        p.getCreatedAt(),
                        p.getViewCount(),
                        p.isHidden()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void togglePostHidden(Long postId) {
        var post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + postId));
        post.setHidden(!post.isHidden());
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        if (!boardPostRepository.existsById(postId)) {
            return;
        }
        boardPostRepository.deleteById(postId);
    }

    // 내부 헬퍼

    private AdminUserDto toAdminUserDto(User u) {
        return new AdminUserDto(
                u.getId(),
                u.getUsername(),
                u.getDisplayName(),
                u.getRole(),
                u.isEnabled()
        );
    }
}

