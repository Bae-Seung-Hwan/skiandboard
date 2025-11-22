package com.springboot.service;

import com.springboot.domain.SkiResort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficServiceImpl implements TrafficService {

    @Value("${traffic.its.api-key}")
    private String apiKey;

    @Value("${traffic.its.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();
    private double lastAvgSpeed = 0.0; // 최근 속도

    @Override
    public double getLastAvgSpeed() {
        return lastAvgSpeed;
    }

    @Override
    public int estimateTrafficLevel(SkiResort resort) {

        Double lat = resort.getLat();
        Double lng = resort.getLng();

        if (lat == null || lng == null) return 2;

        // 주변 box 범위 계산
        double delta = 0.05;
        double minX = lng - delta;
        double maxX = lng + delta;
        double minY = lat - delta;
        double maxY = lat + delta;

        String url = baseUrl +
                "?apiKey=" + apiKey +
                "&type=all" +
                "&routeNo=0" +
                "&drcType=all" +
                "&minX=" + minX +
                "&maxX=" + maxX +
                "&minY=" + minY +
                "&maxY=" + maxY +
                "&getType=json";

        Map<String, Object> json = restClient.get()
                .uri(url)
                .retrieve()
                .body(Map.class);

        List<Double> speeds = extractSpeeds(json);
        if (speeds.isEmpty()) return 2;

        double avgSpeed = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        lastAvgSpeed = avgSpeed;

        if (avgSpeed <= 15) return 5;
        if (avgSpeed <= 30) return 4;
        if (avgSpeed <= 50) return 3;
        if (avgSpeed <= 70) return 2;
        return 1;
    }

    @SuppressWarnings("unchecked")
    private List<Double> extractSpeeds(Map<String, Object> json) {
        List<Double> speeds = new ArrayList<>();

        try {
            Map<String, Object> body = (Map<String, Object>) json.get("body");
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            for (Map<String, Object> item : items) {
                if (item.get("speed") != null) {
                    double s = Double.parseDouble(item.get("speed").toString());
                    if (s > 0) speeds.add(s);
                }
            }
        } catch (Exception ignored) {}

        return speeds;
    }
}

