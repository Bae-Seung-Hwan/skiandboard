package com.springboot.service;

import com.springboot.dto.WeatherDto;

public interface WeatherService {
    WeatherDto getWeatherForResort(Long resortId);
}
