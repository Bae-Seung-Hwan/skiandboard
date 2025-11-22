package com.springboot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "traffic_cache")
public class TrafficCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long resortId;

    private int level; // 1~5

    private double avgSpeed;

    private Instant fetchedAt;
}
