package com.springboot.dto;

public record CongestionDto(
    Long resortId,
    String resortName,
    int level,     // 1~5
    String label,  // LOW / MODERATE / HIGH / VERY_HIGH / EXTREME
    String reason  // 설명 (주말/시간대/신설/기온/인기 등)
) {}
