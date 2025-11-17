package com.springboot.controller;

import com.springboot.dto.CongestionDto;
import com.springboot.service.CongestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/congestion")//api 적용
@RequiredArgsConstructor
public class CongestionController {

    private final CongestionService congestionService;

    @GetMapping("/{resortId}") 
    public CongestionDto get(@PathVariable("resortId") Long resortId) {
        return congestionService.estimate(resortId);
    }
}
