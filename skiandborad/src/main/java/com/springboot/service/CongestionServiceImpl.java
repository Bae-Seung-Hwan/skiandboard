package com.springboot.service;

import com.springboot.domain.SkiResort;
import com.springboot.dto.CongestionDto;
import com.springboot.dto.WeatherDto;
import com.springboot.repository.SkiResortRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CongestionServiceImpl implements CongestionService {

    private final SkiResortRepository skiResortRepository;
    private final WeatherService weatherService;
    private final TrafficService trafficService;

    @Override
    public CongestionDto estimate(Long resortId) {

        SkiResort resort = skiResortRepository.findById(resortId)
                .orElseThrow(() -> new IllegalArgumentException("리조트를 찾을 수 없습니다. id=" + resortId));

        double rating = resort.getOverallRating() != null ? resort.getOverallRating() : 3.0;

        WeatherDto w;
        try {
            w = weatherService.getWeatherForResort(resortId);
        } catch (Exception e) {
            log.warn("혼잡도 계산 중 날씨 조회 실패, 기본값 사용. resortId={}, msg={}", resortId, e.getMessage());
            w = new WeatherDto(
                    resortId,
                    resort.getName(),
                    0.0, 0.0, 0.0,
                    "UNKNOWN",
                    java.time.Instant.now()
            );
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        DayOfWeek dow = today.getDayOfWeek();
        boolean weekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);

        int score = 2; // 기본 보통

        // 요일
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            score += 1;  // 주말이면 +1만
        }

        // 시간대
        LocalTime coreStart = LocalTime.of(9,0);
        LocalTime coreEnd   = LocalTime.of(16,0);
        LocalTime morningStart = LocalTime.of(7,0);
        LocalTime eveningEnd   = LocalTime.of(19,0);

        if (now.isAfter(coreStart) && now.isBefore(coreEnd)) {
            score += 1; // 핵심 시간대
        } else if (
                (now.isAfter(morningStart) && now.isBefore(coreStart)) ||
                (now.isAfter(coreEnd) && now.isBefore(eveningEnd))
        ) {
        } else {
            score -= 1;
        }

        // 적설
        double snow = w.snowfallCm();
        if (snow >= 10) score -= 1;
        else if (snow >= 3) score += 1;

        // 기온
        double temp = w.temperatureC();
        if (temp <= -12) score -= 2;
        else if (temp <= -5) score -= 1;
        else if (temp >= 3)  score -= 1;

        // 인기도(평점)
        if (rating >= 4.5) score += 2;
        else if (rating >= 4.0) score += 1;
        else if (rating <= 2.5) score -= 1;

        // 교통 (ITS 기반)
        int trafficLevel = trafficService.estimateTrafficLevel(resort);
        if (trafficLevel >= 5) {
            score += 2;       // +2
        } else if (trafficLevel == 4) {
            score += 1;       // +1
        } else if (trafficLevel == 3) {
            // 0
        } else if (trafficLevel == 1) {
            score -= 1;
        }

        // 레벨 변환
        int level = Math.max(1, Math.min(5, score));
        double lastSpeed = trafficService.getLastAvgSpeed();
        Double avgSpeed = Double.isNaN(lastSpeed) ? null : lastSpeed;
        Integer travelTime = null;
        String rawStatus = "ITS:Lv" + trafficLevel;
        String label = switch (level) {
            case 1 -> "LOW";
            case 2 -> "MODERATE";
            case 3 -> "HIGH";
            case 4 -> "VERY_HIGH";
            default -> "EXTREME";
        };

        String reason = String.format(
                "요일:%s(%s), 시간대:%s, 기온:%.1f°C, 신설:%.1fcm, 평점:%.1f, 교통:Lv%d",
                dow,
                weekend ? "주말" : "평일",
                now.truncatedTo(ChronoUnit.MINUTES),
                temp,
                snow,
                rating,
                trafficLevel
        );

        return new CongestionDto(
                resort.getId(),
                resort.getName(),
                level,
                label,
                reason,
                avgSpeed,
                travelTime,
                rawStatus
        );
    }

}
