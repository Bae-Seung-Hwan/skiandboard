package com.springboot.dto;

import com.springboot.domain.SkiResort;

public record ResortDto(Long id, String name, String description, Double lat, Double lng) {
    public static ResortDto from(SkiResort r) {
        return new ResortDto(r.getId(), r.getName(), r.getDescription(), r.getLat(), r.getLng());
    }
}
