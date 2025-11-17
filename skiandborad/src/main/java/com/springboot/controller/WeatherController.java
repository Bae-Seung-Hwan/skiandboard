package com.springboot.controller;

import com.springboot.dto.WeatherDto;
import com.springboot.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/{resortId}")
    public WeatherDto get(@PathVariable("resortId") Long resortId) {
        return weatherService.getWeatherForResort(resortId);
    }
}

