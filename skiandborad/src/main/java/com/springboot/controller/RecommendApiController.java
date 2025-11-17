package com.springboot.controller;

import com.springboot.dto.*;
import com.springboot.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendApiController {

  private final RecommendationService recommendationService;

  @GetMapping
  public List<RecommendDto> list(
      @RequestParam(name = "lat") double lat,
      @RequestParam(name = "lng") double lng,
      @RequestParam(name = "skill", required = false) String skill,
      @RequestParam(name = "gear", required = false) String gear,
      @RequestParam(name = "region", required = false) String region,
      @RequestParam(name = "transport", required = false) String transport,
      @RequestParam(name = "maxDistanceKm", required = false) Double maxDistanceKm
  ) {
    return recommendationService.recommend(
        lat, lng,
        SkillLevel.from(skill),
        GearType.from(gear),
        RegionCode.from(region),
        TransportMode.from(transport),
        maxDistanceKm
    );
  }
}
