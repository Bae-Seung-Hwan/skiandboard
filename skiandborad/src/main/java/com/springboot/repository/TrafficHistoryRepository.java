package com.springboot.repository;

import com.springboot.domain.SkiResort;
import com.springboot.domain.TrafficHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TrafficHistoryRepository extends JpaRepository<TrafficHistory, Long> {

    List<TrafficHistory> findByResortAndRecordedAtBetweenOrderByRecordedAtAsc(
            SkiResort resort,
            LocalDateTime start,
            LocalDateTime end
    );
}
