package com.springboot.service;

import com.springboot.dto.TrafficHistoryPointDto;

import java.util.List;

public interface TrafficHistoryService {

    // 이미 있는 메서드
    void collectSnapshotForAllResorts();

    // ✅ 새로 추가: 특정 스키장의 최근 N시간 혼잡도/속도 기록 조회
    List<TrafficHistoryPointDto> getHistory(Long resortId, int hours);
}
