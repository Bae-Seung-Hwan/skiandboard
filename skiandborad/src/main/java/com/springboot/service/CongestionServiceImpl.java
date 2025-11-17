package com.springboot.service;

import com.springboot.dto.CongestionDto;
import com.springboot.repository.SkiResortRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
//혼잡도 점수 매기기
@Service
@RequiredArgsConstructor
public class CongestionServiceImpl implements CongestionService {

    private final SkiResortRepository resortRepo;
    private final WeatherService weatherService; // 날씨 기반 가중치 활용

    @Override
    public CongestionDto estimate(Long resortId) {
        var r = resortRepo.findById(resortId).orElseThrow();
        var w = weatherService.getWeatherForResort(resortId);

        int score = 0;
        var dow = LocalDate.now().getDayOfWeek();
        var now = LocalTime.now();

        boolean weekend = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
        if (weekend) score += 2; // 주말 가중치
        if (now.isAfter(LocalTime.of(9,0)) && now.isBefore(LocalTime.of(15,0))) score += 1; // 피크타임
        if (w.snowfallCm() > 3) score += 1; // 신설 눈 → 붐빔 경향
        if (Math.abs(w.temperatureC()) < 3) score += 1; // 포근한 날 → 이용 증가

        // 리조트 인기(선택): SkiResort에 popularityScore가 있으면 1~2점 가중
        int popularity = 3;
        try {
            var m = r.getClass().getMethod("getPopularityScore");
            var val = (Integer) m.invoke(r);
            if (val != null) popularity = Math.max(1, Math.min(5, val));
        } catch (Exception ignore) {}
        score += Math.min(2, popularity / 2);

        int level = Math.max(1, Math.min(5, score));
        String label = switch (level) {
            case 1 -> "LOW";
            case 2 -> "MODERATE";
            case 3 -> "HIGH";
            case 4 -> "VERY_HIGH";
            default -> "EXTREME";
        };
        String reason = String.format("주말:%s, 시간대:%s, 신설:%.1fcm, 기온:%.1f°C, 인기:%d",
                weekend, now, w.snowfallCm(), w.temperatureC(), popularity);

        return new CongestionDto(r.getId(), r.getName(), level, label, reason);
    }
}
