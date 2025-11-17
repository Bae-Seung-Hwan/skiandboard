package com.springboot.dto;

import com.springboot.domain.SkiResort;

public record ResortDetailDto(
        Long id,
        String name,
        String description,        // 메인 페이지 요약
        String longDescription,    // 상세 페이지 설명
        String homepageUrl,
        String heroImageUrl,
        String priceInfo,
        Double overallRating,
        Double facilityScore,
        Double slopeScore,
        Double accessibilityScore,
        Double snowQualityScore
) {
    public static ResortDetailDto from(SkiResort r) {
        return new ResortDetailDto(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getLongDescription(),
                r.getHomepageUrl(),
                r.getHeroImageUrl(),
                r.getPriceInfo(),
                r.getOverallRating(),
                r.getFacilityScore(),
                r.getSlopeScore(),
                r.getAccessibilityScore(),
                r.getSnowQualityScore()
        );
    }
}
