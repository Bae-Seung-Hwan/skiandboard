package com.springboot.repository;

import com.springboot.domain.BoardCategory;
import com.springboot.domain.BoardPost;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

  // 목록 기본: 작성자 즉시 로딩 (N+1 방지)
  @EntityGraph(attributePaths = "author")
  Page<BoardPost> findAll(Pageable pageable);

  @EntityGraph(attributePaths = "author")
  Page<BoardPost> findByCategory(BoardCategory category, Pageable pageable);

  @EntityGraph(attributePaths = "author")
  Page<BoardPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
      String title, String content, Pageable pageable);

  // 카테고리 + 검색 동시
  @EntityGraph(attributePaths = "author")
  Page<BoardPost> findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndContentContainingIgnoreCase(
      BoardCategory category1, String title,
      BoardCategory category2, String content,
      Pageable pageable);

  // 조회수 증가
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update BoardPost p set p.viewCount = p.viewCount + 1 where p.id = :id")
  void incrementViewCount(@Param("id") Long id);
  @Query("""
	       select p
	       from BoardPost p
	       left join fetch p.author
	       where p.id = :id
	       """)
	java.util.Optional<BoardPost> findByIdWithAuthor(@Param("id") Long id);
}
