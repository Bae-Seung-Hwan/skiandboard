package com.springboot.service;

import com.springboot.domain.SkiResort;

public interface TrafficService {

    /**
     * ITS 교통소통정보 기반
     * 1(매우 원활) ~ 5(매우 정체)
     */
    int estimateTrafficLevel(SkiResort resort);
    double getLastAvgSpeed();
}
