package com.springboot.repository;

import com.springboot.domain.WeatherCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface WeatherCacheRepository extends JpaRepository<WeatherCache, Long> {
    Optional<WeatherCache> findFirstByResortIdAndFetchedAtAfterOrderByFetchedAtDesc(Long resortId, Instant cutoff);
}
