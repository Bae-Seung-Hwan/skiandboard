package com.springboot.service;

import com.springboot.dto.*;
import com.springboot.repository.SkiResortRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final SkiResortRepository resortRepo;
    private final WeatherService weatherService;
    private final CongestionService congestionService;

    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private String getResortRegionSafe(Object resort) {
        try {
            Method m = resort.getClass().getMethod("getRegion");
            Object v = m.invoke(resort);
            return v == null ? null : v.toString();
        } catch (Exception ignored) { return null; }
    }

    private int getIntSafe(Object resort, String getter, int fallback) {
        try {
            Method m = resort.getClass().getMethod(getter);
            Object v = m.invoke(resort);
            if (v instanceof Number n) return n.intValue();
        } catch (Exception ignored) {}
        return fallback;
    }

    private double skillAffinity(SkillLevel skill, int elevation, int slopeCount) {
        int sizeScore = Math.min(5, (elevation / 300) + (slopeCount / 5)); // 0~5
        return switch (skill) {
            case BEGINNER -> (5 - sizeScore) * 0.6 + 1.0;
            case INTERMEDIATE -> 1.0 + Math.abs(3 - sizeScore) * 0.1;
            case ADVANCED -> sizeScore * 0.35 + 1.0;
            case EXPERT -> sizeScore * 0.5 + 1.0;
        };
    }

    private double gearAffinity(GearType gear) {
        return switch (gear) { case SKI -> 1.0; case SNOWBOARD -> 1.05; case BOTH -> 1.02; };
    }

    private boolean regionMatches(RegionCode want, String regionStr) {
        if (want == RegionCode.ANY) return true;
        if (regionStr == null) return true;
        String v = regionStr.toUpperCase();
        return switch (want) {
            case SEOUL_GYEONGGI -> v.contains("SEOUL") || v.contains("GYEONGGI") || v.contains("수도권") || v.contains("서울") || v.contains("경기");
            case GANGWON -> v.contains("GANGWON") || v.contains("강원");
            case CHUNGCHEONG -> v.contains("CHUNGCHEONG") || v.contains("충청");
            case JEOLLA -> v.contains("JEOLLA") || v.contains("전라");
            case GYEONGSANG -> v.contains("GYEONGSANG") || v.contains("경상");
            case JEJU -> v.contains("JEJU") || v.contains("제주");
            default -> true;
        };
    }

    private double transportDistanceFactor(TransportMode mode, double distanceKm) {
        return switch (mode) {
            case CAR -> Math.max(0, 120 - distanceKm) / 25.0;
            case PUBLIC -> Math.max(0, 90 - distanceKm) / 25.0;
            case SHUTTLE -> Math.max(0, 100 - distanceKm) / 25.0;
        };
    }

    @Override
    public List<RecommendDto> recommend(double userLat, double userLng,
                                        SkillLevel skill, GearType gear,
                                        RegionCode region, TransportMode transport,
                                        Double maxDistanceKm) {

        var resorts = resortRepo.findAll();

        return resorts.stream()
            // 좌표 없는 리조트는 제외
            .filter(r -> r.getLat() != null && r.getLng() != null)
            // Optional로 감싸서 null 안전 처리 → flatMap으로 펼침
            .flatMap(r -> {
                try {
                    double distance = haversine(userLat, userLng, r.getLat(), r.getLng());

                    // region / 거리 필터
                    String regionStr = getResortRegionSafe(r);
                    if (!regionMatches(region, regionStr)) return Stream.empty();
                    if (maxDistanceKm != null && distance > maxDistanceKm) return Stream.empty();

                    var w = weatherService.getWeatherForResort(r.getId());
                    var c = congestionService.estimate(r.getId());

                    int elevation = getIntSafe(r, "getElevation", 600);
                    int slopeCount = getIntSafe(r, "getSlopeCount", 8);

                    double weatherScore = (w.snowfallCm() * 2.0)
                            + (w.temperatureC() > -6 && w.temperatureC() < 2 ? 1.5 : 0.5);
                    double crowdBonus = (6 - c.level()) * 1.2;
                    double distanceBonus = transportDistanceFactor(transport, distance);
                    double skillBonus = skillAffinity(skill, elevation, slopeCount);
                    double gearBonus = gearAffinity(gear);

                    double score = (weatherScore + crowdBonus + distanceBonus) * skillBonus * gearBonus;

                    String summary = String.format(
                        "거리 %.1fkm · 혼잡 %d/5(%s) · 신설 %.1fcm · %.1f°C · 숙련 %.0f%% · 교통 %s",
                        distance, c.level(), c.label(), w.snowfallCm(), w.temperatureC(),
                        (skillBonus - 1.0) * 100, transport
                    );

                    return Stream.of(new RecommendDto(
                        r.getId(), r.getName(), score,
                        c.level(), distance, w.temperatureC(), w.snowfallCm(), summary
                    ));
                } catch (Exception ex) {
                    // 어떤 리조트에서라도 예외가 나면 그 항목만 건너뜀 (전체 500 방지)
                    return Stream.empty();
                }
            })
            .sorted(Comparator.comparingDouble(RecommendDto::score).reversed())
            .limit(10)
            .toList();
    }
}

