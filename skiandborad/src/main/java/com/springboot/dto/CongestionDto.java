package com.springboot.dto;

public record CongestionDto(
    Long resortId,
    String resortName,
    int level,          // 1~5
    String label,       // LOW / MODERATE / HIGH / VERY_HIGH / EXTREME
    String reason,      // 설명 (주말/시간대/신설/기온/인기 등)

    // 새로 추가: 교통 관련 원시 데이터
    Double avgSpeed,    // 평균 속도 (km/h) - ITS 데이터에서 가져오거나 일단 null
    Integer travelTime, // 소요 시간 (초/분 단위, 없으면 null)
    String rawStatus    // ITS 원본 상태 문자열 (예: "ITS:Lv3" 등)
) {}
