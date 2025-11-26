package com.springboot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "traffic_history",
       indexes = {
           @Index(name = "idx_history_resort_time", columnList = "resort_id, recordedAt")
       })
public class TrafficHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 스키장 데이터인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id", nullable = false)
    private SkiResort resort;

    // 이 스냅샷을 기록한 시각
    @Column(nullable = false)
    private LocalDateTime recordedAt;

    // 혼잡도 레벨 (1~5)
    @Column(nullable = false)
    private int congestionLevel;

    // 평균 속도 (km/h) - null 허용
    private Double speed;

    // 소요 시간 (초/분 등) - null 허용
    private Integer travelTime;

    // ITS 원본 상태 (예: "ITS:Lv3" 등)
    @Column(length = 50)
    private String rawStatus;

    // 데이터 출처
    @Column(length = 20)
    private String source;  // "ITS"
}
