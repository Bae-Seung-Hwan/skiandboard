package com.springboot.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user") // user 대신 안전하게
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false, unique=true, length=50)
  private String username;

  @Column(nullable=false, length=100)
  private String password; // BCrypt

  @Column(nullable=false, length=100)
  private String displayName;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role = Role.USER;

  @Builder.Default
  @Column(nullable = false)
  private boolean enabled = true;

  @PrePersist
  void prePersist() {
    if (role == null) role = Role.USER;
  }
  
}
