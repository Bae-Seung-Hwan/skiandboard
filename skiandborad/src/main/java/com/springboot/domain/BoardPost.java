package com.springboot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "board_post")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class BoardPost {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // FK: user 삭제 시 SET NULL
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "author_id")
  private User author;

  @Column(nullable = false, length = 120)
  private String title;

  @Lob @Column(nullable = false)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BoardCategory category;

  // DB가 채움: TIMESTAMP -> Instant
  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private Instant updatedAt;

  @Column(name = "view_count", nullable = false)
  private long viewCount;
  
  @Column(nullable = false)
  private boolean hidden = false;
}
