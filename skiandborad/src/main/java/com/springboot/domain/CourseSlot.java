package com.springboot.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseSlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;      // 예: "오전 초급 코스"
    private String timeRange;  // 예: "09:00 ~ 11:00"
    private String note;       // 간단 설명
    private Integer displayOrder; // 정렬순서
}