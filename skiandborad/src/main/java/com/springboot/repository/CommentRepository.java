package com.springboot.repository;

import com.springboot.domain.BoardPost;
import com.springboot.domain.Comment;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @EntityGraph(attributePaths = "author")
  List<Comment> findByPostOrderByCreatedAtAsc(BoardPost post);
  // 마이페이지용: 특정 사용자의 최신 댓글 5개
  @EntityGraph(attributePaths = {"author", "post"})
  List<Comment> findTop5ByAuthor_UsernameOrderByCreatedAtDesc(String username);
  // 마이페이지용: 특정 사용자의 전체 댓글
  @EntityGraph(attributePaths = {"author", "post"})
  List<Comment> findByAuthor_UsernameOrderByCreatedAtDesc(String username);
}
