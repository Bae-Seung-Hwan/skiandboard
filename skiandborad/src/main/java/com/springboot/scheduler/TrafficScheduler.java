package com.springboot.scheduler;

import com.springboot.domain.SkiResort;
import com.springboot.domain.TrafficCache;
import com.springboot.repository.SkiResortRepository;
import com.springboot.repository.TrafficCacheRepository;
import com.springboot.service.TrafficService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficScheduler {

    private final SkiResortRepository resortRepository;
    private final TrafficService trafficService;
    private final TrafficCacheRepository trafficCacheRepository;

    /**
     * 2분마다 전체 스키장 교통 정보 업데이트
     */
    @Scheduled(fixedRate = 120_000) // 2 minutes
    public void updateTrafficCache() {
        List<SkiResort> resorts = resortRepository.findAll();

        for (SkiResort resort : resorts) {
            try {
                int level = trafficService.estimateTrafficLevel(resort);
                double speed = trafficService.getLastAvgSpeed(); // 추가 메서드 설명 아래

                TrafficCache cache = TrafficCache.builder()
                        .resortId(resort.getId())
                        .level(level)
                        .avgSpeed(speed)
                        .fetchedAt(Instant.now())
                        .build();

                trafficCacheRepository.save(cache);

                log.info("Updated traffic cache for {} → L{}, speed={}",
                        resort.getName(), level, speed);

            } catch (Exception e) {
                log.warn("Traffic update failed for {}: {}", resort.getName(), e.getMessage());
            }
        }
    }
}
