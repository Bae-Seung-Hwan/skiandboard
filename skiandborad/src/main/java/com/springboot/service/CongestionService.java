package com.springboot.service;

import com.springboot.dto.CongestionDto;

public interface CongestionService {
    CongestionDto estimate(Long resortId);
}
