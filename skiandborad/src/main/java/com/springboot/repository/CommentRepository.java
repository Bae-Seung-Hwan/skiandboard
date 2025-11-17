package com.springboot.repository;

import com.springboot.domain.BoardPost;
import com.springboot.domain.Comment;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @EntityGraph(attributePaths = "author")
  List<Comment> findByPostOrderByCreatedAtAsc(BoardPost post);
}
