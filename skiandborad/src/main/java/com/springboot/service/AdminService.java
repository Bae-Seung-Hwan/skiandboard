package com.springboot.service;

import com.springboot.dto.AdminUserDto;
import com.springboot.dto.AdminUserEditRequest;
import com.springboot.dto.PostListItemDto;
import com.springboot.dto.ResortDto;

import java.util.List;

public interface AdminService {

    // 대시보드 요약
    long countUsers();
    long countResorts();
    long countPosts();
    long countComments();

    List<AdminUserDto> getRecentUsers(int limit);
    List<PostListItemDto> getRecentPosts(int limit);

    // 목록 페이지용
    List<AdminUserDto> getAllUsers();
    List<ResortDto> getAllResorts();
    // 회원 관리 CRUD
    AdminUserEditRequest getUserForEdit(Long userId);
    void updateUser(Long userId, AdminUserEditRequest req);
    void toggleUserEnabled(Long userId);
    void deleteUser(Long userId);
    
    List<PostListItemDto> getAllPostsForAdmin();

    void togglePostHidden(Long postId);

    void deletePost(Long postId);
}
