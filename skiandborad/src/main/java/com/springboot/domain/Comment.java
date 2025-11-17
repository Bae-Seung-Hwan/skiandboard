package com.springboot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "comment")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Comment {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "post_id", nullable = false)
  private BoardPost post;

  @ManyToOne(fetch = FetchType.LAZY, optional = true) @JoinColumn(name = "author_id")
  private User author;

  @Lob @Column(nullable = false)
  private String content;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;
}
