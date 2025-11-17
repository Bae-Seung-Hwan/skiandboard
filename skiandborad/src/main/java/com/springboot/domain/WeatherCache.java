package com.springboot.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="weather_cache", indexes = {
    @Index(name="idx_weather_resort_time", columnList="resort_id,fetched_at")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WeatherCache {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="resort_id", nullable=false)
    private Long resortId;

    @Column(name="temperature_c")
    private double temperatureC;

    @Column(name="wind_ms")
    private double windMs;

    @Column(name="snowfall_cm")
    private double snowfallCm;

    @Column(name = "wx_condition", length = 64)
    private String condition;

    @Column(name="fetched_at", nullable=false)
    private Instant fetchedAt;
}
