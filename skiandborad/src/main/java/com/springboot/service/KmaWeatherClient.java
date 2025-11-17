package com.springboot.service;

import com.springboot.dto.WeatherDto;

public interface KmaWeatherClient {
    WeatherDto getWeather(double lat, double lon);
}
