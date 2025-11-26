package com.springboot.scheduler;

import com.springboot.service.TrafficHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficHistoryScheduler {

    private final TrafficHistoryService trafficHistoryService;

    // 일단 디버깅용으로 1분마다 돌리기
    @Scheduled(cron = "0 * * * * *")   // 30분마다는 "0 */30 * * * *"
    public void collectTrafficHistory() {
        log.info("[TrafficHistoryScheduler] 혼잡도 스냅샷 수집 시작");
        trafficHistoryService.collectSnapshotForAllResorts();
        log.info("[TrafficHistoryScheduler] 혼잡도 스냅샷 수집 완료");
    }
}

