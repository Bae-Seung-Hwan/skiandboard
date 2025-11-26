package com.springboot.dto;

import java.time.LocalDateTime;

public record TrafficHistoryPointDto(
        LocalDateTime recordedAt, // 기록 시각
        int congestionLevel,      // 혼잡도 레벨 (1~5)
        Double speed              // 평균 속도 (km/h, 없으면 null)
) {}
