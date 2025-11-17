package com.springboot.repository;

import com.springboot.domain.ResortType;
import com.springboot.domain.SkiResort;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkiResortRepository extends JpaRepository<SkiResort, Long> {

    List<SkiResort> findByType(ResortType type);

    // 좌표 있는 것만 바로 가져오고 싶을 때
    List<SkiResort> findByTypeAndLatIsNotNullAndLngIsNotNull(ResortType type);

    // ✅ 홈화면에 최근 6개 스키장 표시용
    List<SkiResort> findTop6ByOrderByIdDesc();
}
