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

    private final SkiResortRepository resortRepo; //추천 로직에서 리조트 기본 정보를 조회
    private final WeatherService weatherService; //키새기반 외부 OpenWeather,KMA api 사용
    private final CongestionService congestionService; //리조트 줍젼 혼잡도 조회 서비스

    //두 좌표 간 거리 계산(Haversine 공식)
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; //지구 반지름
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
    //리플렉션으로 안전하게 리조트 지역명을 가져오는 함수
    private String getResortRegionSafe(Object resort) {
        try {
            Method m = resort.getClass().getMethod("getRegion");
            Object v = m.invoke(resort);
            return v == null ? null : v.toString();
        } catch (Exception ignored) { return null; }
    }
    // 리조트의 필드(elevation, slopeCount 등)를 안전하게 가져오는 유틸 실패시 fallback값 사용
    private int getIntSafe(Object resort, String getter, int fallback) {
        try {
            Method m = resort.getClass().getMethod(getter);
            Object v = m.invoke(resort);
            if (v instanceof Number n) return n.intValue();
        } catch (Exception ignored) {}
        return fallback;
    }
    //실력(SkillLevel)에 따른 리조트 적합도 점수 계산
    //초보자(BEGINNER)는 너무 큰 리조트 감점
    //숙련자(EXPERT)는 규모가 크면 가산점
    private double skillAffinity(SkillLevel skill, int elevation, int slopeCount) {
        int sizeScore = Math.min(5, (elevation / 300) + (slopeCount / 5)); // 0~5
        return switch (skill) {
            case BEGINNER -> (5 - sizeScore) * 0.6 + 1.0;
            case INTERMEDIATE -> 1.3 - Math.abs(3 - sizeScore) * 0.1;
            case ADVANCED -> sizeScore * 0.35 + 1.0;
            case EXPERT -> sizeScore * 0.5 + 1.0;
        };
    }
    //장비에 따른 미세 가중치
    private double gearAffinity(GearType gear) {
        return switch (gear) { case SKI -> 1.0; case SNOWBOARD -> 1.05; case BOTH -> 1.02; };
    }
    //선호 지역 코드가 리조트 지역명에 포함되는지 체크
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
    //이동수단 별 거리 패널티 및 가산점
    private double transportDistanceFactor(TransportMode mode, double distanceKm) {
        return switch (mode) {
            case CAR -> Math.max(0, 120 - distanceKm) / 25.0;
            case PUBLIC -> Math.max(0, 90 - distanceKm) / 25.0;
            case SHUTTLE -> Math.max(0, 100 - distanceKm) / 25.0;
        };
    }
    //사용자 조건을 기반으로 리조트 추천
    //거리, 날씨, 혼잡도, 실력, 장비 등으로 종합 점수로 계산
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
                    //지역 필터링
                    String regionStr = getResortRegionSafe(r);
                    if (!regionMatches(region, regionStr)) return Stream.empty();
                    
                    //최대 거리 제한이 있을때 필터링
                    if (maxDistanceKm != null && distance > maxDistanceKm) return Stream.empty();
                    //날씨 혼잡도 조회
                    var w = weatherService.getWeatherForResort(r.getId());
                    var c = congestionService.estimate(r.getId());
                    //고도, 슬로프 수 안전 조회
                    int elevation = getIntSafe(r, "getElevation", 600);
                    int slopeCount = getIntSafe(r, "getSlopeCount", 8);
                    //점수 계산
                    //적설량, 기온 기반 점수
                    double weatherScore = (w.snowfallCm() * 2.0)
                            + (w.temperatureC() > -6 && w.temperatureC() < 2 ? 1.5 : 0.5);
                    //혼잡도 낮을수록 점수 증가
                    double crowdBonus = (6 - c.level()) * 1.2;
                    //이동수단 별 거리 가중치
                    double distanceBonus = transportDistanceFactor(transport, distance);
                    //실력/ 장비 기반 가중치
                    double skillBonus = skillAffinity(skill, elevation, slopeCount);
                    double gearBonus = gearAffinity(gear);
                    
                    //최종 점수
                    double score = (weatherScore + crowdBonus + distanceBonus) * skillBonus * gearBonus;

                    //프론트 표시용
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
            //점수 내림차순 정렬
            .sorted(Comparator.comparingDouble(RecommendDto::score).reversed())
            //상위 10개만 사용(나중에 리조트 추가용)
            .limit(10)
            .toList();
    }
}

