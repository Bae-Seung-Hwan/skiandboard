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

    @Value("${traffic.its.api-key}") //ITS(국토부 교통정보) api key(application.yml 에서 주입)
    private String apiKey;

    @Value("${traffic.its.base-url}") //ITS API 기본 URL
    private String baseUrl;

    private final RestClient restClient = RestClient.create();
    private double lastAvgSpeed = 0.0; // 최근 속도

    @Override
    public double getLastAvgSpeed() {
        return lastAvgSpeed;
    }
    /**
     * 레벨 정의:
     *  1 : 매우 원활 (평균속도 70km/h 이상)
     *  2 : 원활   (~70)
     *  3 : 보통   (~50)
     *  4 : 혼잡   (~30)
     *  5 : 매우 혼잡 (~15 이하)
     */
    @Override
    public int estimateTrafficLevel(SkiResort resort) {

        Double lat = resort.getLat();
        Double lng = resort.getLng();
        // 좌표가 없는 리조트는 기본 레벨(2레벨)로 반환
        if (lat == null || lng == null) return 2;

        // 주변 box 범위 계산
        double delta = 0.05;
        double minX = lng - delta;
        double maxX = lng + delta;
        double minY = lat - delta;
        double maxY = lat + delta;
        //api 요청 url 구성
        String url = baseUrl +
                "?apiKey=" + apiKey +
                "&type=all" +
                "&routeNo=0" +
                "&drcType=all" +
                "&minX=" + minX +
                "&maxX=" + maxX +
                "&minY=" + minY +
                "&maxY=" + maxY +
                "&getType=json"; //json으로 응답
        
        //api 호출+json 파싱
        Map<String, Object> json = restClient.get()
                .uri(url)
                .retrieve()
                .body(Map.class);
        //속도 목록 추출
        List<Double> speeds = extractSpeeds(json);
        if (speeds.isEmpty()) return 2;
        //평균 속도 계산
        double avgSpeed = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        lastAvgSpeed = avgSpeed;

        if (avgSpeed <= 15) return 5; //매우 혼잡
        if (avgSpeed <= 30) return 4; //혼잡
        if (avgSpeed <= 50) return 3; //보통
        if (avgSpeed <= 70) return 2; //원활
        return 1; //매우 원활
    }
    //json구조에서 speed값만 추출
    @SuppressWarnings("unchecked")
    private List<Double> extractSpeeds(Map<String, Object> json) {
        List<Double> speeds = new ArrayList<>();

        try {
            Map<String, Object> body = (Map<String, Object>) json.get("body");
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            for (Map<String, Object> item : items) {
                if (item.get("speed") != null) {
                    double s = Double.parseDouble(item.get("speed").toString());
                    if (s > 0) speeds.add(s); //0속도는 제외
                }
            }
        } catch (Exception ignored) {}// JSON 구조가 변경되거나 null일 경우 빈 리스트 반환

        return speeds;
    }
}

