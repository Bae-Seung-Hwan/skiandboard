package com.springboot.service;

import com.springboot.domain.SkiResort;
import com.springboot.domain.WeatherCache;
import com.springboot.dto.WeatherDto;
import com.springboot.repository.SkiResortRepository;
import com.springboot.repository.WeatherCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private final SkiResortRepository resortRepo;
    private final WeatherCacheRepository cacheRepo;
    private final KmaWeatherClient kmaWeatherClient;
    private final OpenWeatherClient openWeatherClient;

    @Value("${weather.cache-ttl-minutes:30}")
    private long cacheTtlMinutes;

    @Override
    public WeatherDto getWeatherForResort(Long resortId) {

        // 리조트 조회
        SkiResort resort = resortRepo.findById(resortId)
                .orElseThrow(() -> new IllegalArgumentException("리조트를 찾을 수 없습니다. id=" + resortId));

        if (resort.getLat() == null || resort.getLng() == null) {
            throw new IllegalStateException("위도/경도 정보가 없는 리조트입니다. id=" + resortId);
        }

        // 캐시 유효기간 기준 시각 계산 (지금 - TTL분)
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(cacheTtlMinutes));

        // 캐시 조회 (WeatherCacheRepository에 실제로 있는 메서드 사용)
        WeatherCache cache = cacheRepo
                .findFirstByResortIdAndFetchedAtAfterOrderByFetchedAtDesc(resortId, cutoff)
                .orElseGet(() -> {
                    WeatherCache fresh = fetchFromProvider(resort);
                    return cacheRepo.save(fresh);
                });

        // 캐시(또는 방금 저장한 fresh)를 DTO로 변환
        return new WeatherDto(
                resort.getId(),
                resort.getName(),
                cache.getTemperatureC(),
                cache.getWindMs(),
                cache.getSnowfallCm(),
                cache.getCondition(),
                cache.getFetchedAt()
        );
    }

    
     //외부 제공자(KMA 실패 시 OpenWeather)에서 날씨를 받아와 WeatherCache 엔티티를 만들어줌
     
    private WeatherCache fetchFromProvider(SkiResort resort) {
        WeatherDto dto = tryKmaOrFallback(resort.getLat(), resort.getLng());

        WeatherCache cache = new WeatherCache();
        cache.setResortId(resort.getId());
        cache.setTemperatureC(dto.temperatureC());
        cache.setWindMs(dto.windMs());
        cache.setSnowfallCm(dto.snowfallCm());
        cache.setCondition(dto.condition());
        cache.setFetchedAt(dto.fetchedAt());

        return cache;
    }

    
     //KMA 호출 실패하면 OpenWeather로 폴백
     
    private WeatherDto tryKmaOrFallback(double lat, double lng) {
        // 1차: KMA
        try {
            return kmaWeatherClient.getWeather(lat, lng);
        } catch (Exception e) {
            log.warn("KMA 호출 실패, OpenWeather 폴백 사용. msg={}", e.getMessage());
        }

        // 2차: OpenWeather
        try {
            return openWeatherClient.getWeather(lat, lng);
        } catch (Exception e) {
            log.error("OpenWeather 호출도 실패했습니다. msg={}", e.getMessage());

            // 3차: 완전 폴백 (더미 데이터)
            return new WeatherDto(
                    null,
                    null,
                    0.0,
                    0.0,
                    0.0,
                    "ERROR",
                    Instant.now()
            );
        }
    }


}

