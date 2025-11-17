package com.springboot.dto;

public record MainResortCardDto(
        Long id, String name, String description, String heroImageUrl, Double lat, Double lng
) {}