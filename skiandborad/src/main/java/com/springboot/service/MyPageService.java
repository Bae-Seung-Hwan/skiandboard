package com.springboot.service;

import com.springboot.dto.*;
import java.util.List;

public interface MyPageService {

  /** 마이페이지 요약: 최근 글/댓글 몇 개 */
  List<PostListItemDto> getRecentPosts(String username);

  List<CommentDto> getRecentComments(String username);

  /** 내 글 전체 */
  List<PostListItemDto> getAllPosts(String username);

  /** 내 댓글 전체 */
  List<MyCommentDto> getAllComments(String username);

  /** 프로필 조회/수정 */
  ProfileUpdateRequest getProfile(String username);

  void updateProfile(String username, ProfileUpdateRequest req);
}
