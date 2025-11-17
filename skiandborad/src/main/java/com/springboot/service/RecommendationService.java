package com.springboot.service;

import com.springboot.dto.*;
import java.util.List;

public interface RecommendationService {
    List<RecommendDto> recommend(
        double userLat,
        double userLng,
        SkillLevel skill,
        GearType gear,
        RegionCode region,
        TransportMode transport,
        Double maxDistanceKm // null 허용
    );
}
