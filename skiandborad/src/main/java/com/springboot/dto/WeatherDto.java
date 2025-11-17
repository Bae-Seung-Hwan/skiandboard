package com.springboot.dto;

import java.time.Instant;

public record WeatherDto(
    Long resortId,
    String resortName,
    double temperatureC,
    double windMs,
    double snowfallCm,
    String condition,
    Instant fetchedAt
) {}
