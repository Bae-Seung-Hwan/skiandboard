package com.springboot.service;

import com.springboot.dto.WeatherDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Slf4j
public class KmaWeatherClientImpl implements KmaWeatherClient {

    @Value("${weather.kma.service-key}")
    private String serviceKey;

    private static final String NCST_URL =
            "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

    private static final String FCST_URL =
            "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";

    @Override
    public WeatherDto getWeather(double lat, double lon) {

        var grid = KmaGridConverter.toGrid(lat, lon);

        LocalDateTime now = LocalDateTime.now().minusMinutes(30);
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = now.format(DateTimeFormatter.ofPattern("HH")) + "00";

        Map<String, Double> ncst = callNcst(baseDate, baseTime, grid.nx(), grid.ny());
        Map<String, Double> fcst = callFcst(baseDate, baseTime, grid.nx(), grid.ny());

        double t1h = ncst.getOrDefault("T1H", 0.0);
        double wsd = ncst.getOrDefault("WSD", 0.0);
        double rn1 = ncst.getOrDefault("RN1", 0.0);
        double pty = fcst.getOrDefault("PTY", 0.0);

        // 적설 계산 (간단 버전)
        double snow = (pty == 3 ? rn1 : 0.0);

        String condition =
                (pty == 3) ? "SNOW" :
                (pty == 1 || pty == 2) ? "RAIN" :
                        "CLEAR";

        return new WeatherDto(
                null, null,
                t1h, wsd, snow,
                condition,
                Instant.now()
        );
    }

    private Map<String, Double> callNcst(String baseDate, String baseTime, int nx, int ny) {
        // TODO: Jackson으로 파싱해야 함
        throw new UnsupportedOperationException("KMA NCST JSON 파싱 구현 필요");
    }

    private Map<String, Double> callFcst(String baseDate, String baseTime, int nx, int ny) {
        // TODO: Jackson으로 파싱해야 함
        throw new UnsupportedOperationException("KMA FCST JSON 파싱 구현 필요");
    }
}
