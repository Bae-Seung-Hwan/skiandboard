package com.springboot.service;

import com.springboot.dto.WeatherDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class OpenWeatherClient {

    @Value("${weather.openweather.api-key}")
    private String apiKey;

    @Value("${weather.openweather.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public WeatherDto getWeather(double lat, double lon) {

        String url = baseUrl + "/weather?lat=" + lat +
                "&lon=" + lon +
                "&appid=" + apiKey +
                "&units=metric&lang=kr";

        log.info("Calling OpenWeather API: {}", url);

        Map<String, Object> json = restClient.get()
                .uri(url)
                .retrieve()
                .body(Map.class);

        log.debug("OpenWeather raw response: {}", json);

        Map<String, Object> main  = (Map<String, Object>) json.get("main");              
        Map<String, Object> wind  = (Map<String, Object>) json.get("wind");              
        Map<String, Object> snow  = (Map<String, Object>) json.getOrDefault("snow", Map.of());
        List<Map<String, Object>> weatherArr =
                (List<Map<String, Object>>) json.getOrDefault("weather", List.of());

        double temp     = getDouble(main.get("temp"));
        double windMs   = getDouble(wind.get("speed"));
        double snowfall = getDouble(snow.getOrDefault("1h", 0.0));  // 1시간 적설량 mm 기준

        String condition = "UNKNOWN";
        if (!weatherArr.isEmpty()) {
            Object mainText = weatherArr.get(0).get("main");
            if (mainText != null) {
                condition = mainText.toString().toUpperCase();
            }
        }

        return new WeatherDto(
                null,          // resortId는 WeatherServiceImpl에서 채움
                null,          // resortName도 거기서 채움
                temp,
                windMs,
                snowfall,
                condition,
                Instant.now()
        );
    }

    private double getDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }
}

