package com.springboot.repository;

import com.springboot.domain.TrafficCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrafficCacheRepository extends JpaRepository<TrafficCache, Long> {

    Optional<TrafficCache> findTopByResortIdOrderByFetchedAtDesc(Long resortId);
}
