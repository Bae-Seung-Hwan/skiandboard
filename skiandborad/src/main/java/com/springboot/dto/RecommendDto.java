package com.springboot.dto;

public record RecommendDto(
    Long resortId,
    String resortName,
    double score,
    int congestionLevel,
    double distanceKm,
    double temperatureC,
    double snowfallCm,
    String summary
) {}
