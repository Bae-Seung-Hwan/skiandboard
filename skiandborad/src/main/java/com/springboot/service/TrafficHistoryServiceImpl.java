package com.springboot.service;

import com.springboot.domain.SkiResort;
import com.springboot.domain.TrafficHistory;
import com.springboot.dto.CongestionDto;
import com.springboot.dto.TrafficHistoryPointDto;
import com.springboot.repository.SkiResortRepository;
import com.springboot.repository.TrafficHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficHistoryServiceImpl implements TrafficHistoryService {

    private final SkiResortRepository skiResortRepository;
    private final TrafficHistoryRepository trafficHistoryRepository;
    private final CongestionService congestionService;

    @Override
    @Transactional
    public void collectSnapshotForAllResorts() {
        List<SkiResort> resorts = skiResortRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        log.info("[TrafficHistory] 수집 시작, 리조트 수 = {}", resorts.size());

        for (SkiResort resort : resorts) {
            try {
                CongestionDto dto = congestionService.estimate(resort.getId());

                TrafficHistory history = TrafficHistory.builder()
                        .resort(resort)
                        .recordedAt(now)
                        .congestionLevel(dto.level())
                        .speed(dto.avgSpeed())
                        .travelTime(dto.travelTime())
                        .rawStatus(dto.rawStatus())
                        .source("ITS")
                        .build();

                trafficHistoryRepository.save(history);
                log.info("[TrafficHistory] resortId={} 저장 완료", resort.getId());
            } catch (Exception e) {
                log.warn("[TrafficHistory] 혼잡도 스냅샷 수집 실패 - resortId={}", resort.getId(), e);
            }
        }
    }

    // 차트용 데이터 조회
    @Override
    @Transactional(readOnly = true)
    public List<TrafficHistoryPointDto> getHistory(Long resortId, int hours) {
        SkiResort resort = skiResortRepository.findById(resortId)
                .orElseThrow(() -> new IllegalArgumentException("리조트를 찾을 수 없습니다. id=" + resortId));

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hours);

        List<TrafficHistory> histories =
                trafficHistoryRepository.findByResortAndRecordedAtBetweenOrderByRecordedAtAsc(
                        resort, start, end
                );

        return histories.stream()
                .map(h -> new TrafficHistoryPointDto(
                        h.getRecordedAt(),
                        h.getCongestionLevel(),
                        h.getSpeed()
                ))
                .toList();
    }
}

