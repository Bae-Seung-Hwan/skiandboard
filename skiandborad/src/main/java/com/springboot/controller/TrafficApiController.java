package com.springboot.controller;

import com.springboot.dto.TrafficHistoryPointDto;
import com.springboot.service.TrafficHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resorts")
public class TrafficApiController {

    private final TrafficHistoryService trafficHistoryService;

    /**
     * 특정 스키장의 최근 N시간 교통 혼잡도 / 속도 히스토리 조회
     * 예: GET /api/resorts/1/traffic-history?hours=24
     */
    @GetMapping("/{resortId}/traffic-history")
    public List<TrafficHistoryPointDto> getTrafficHistory(
            @PathVariable("resortId") Long resortId,
            @RequestParam(name = "hours", defaultValue = "24") int hours
    ) {
        if (hours <= 0 || hours > 168) hours = 24;
        return trafficHistoryService.getHistory(resortId, hours);
    }

}
