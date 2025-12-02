package com.springboot.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkiResort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //리조트 id

    private String name; //리조트 이름
    private String description; //설명
    private Double lat; //위도
    private Double lng; //경도

    @Enumerated(EnumType.STRING)
    private ResortType type;

    @Column(name = "homepage_url")
    private String homepageUrl;

    @Column(name = "hero_image_url")
    private String heroImageUrl;
    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;
    // 객관적 평가 항목
    private Double overallRating;        // 종합 점수
    private Double facilityScore;        // 시설
    private Double slopeScore;           // 슬로프
    private Double accessibilityScore;   // 접근성
    private Double snowQualityScore;     // 설질

    private String priceInfo;            // 가격표

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}
