package com.springboot.service;

import com.springboot.domain.ResortType;
import com.springboot.domain.SkiResort;
import com.springboot.dto.ResortDetailDto;
import com.springboot.dto.ResortDto;
import com.springboot.repository.SkiResortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkiResortServiceImpl implements SkiResortService {
	// 리조트 정보를 DB에서 조회하는 JPA 레포지토리
    private final SkiResortRepository repo;
    
    @Override
    public List<ResortDto> list(ResortType type) {
    	// type이 null 이면 전체 조회
        List<SkiResort> resorts =
                (type == null) ? repo.findAll() : repo.findByType(type);
        // 위도 경도가 없는 데이터느 필터링 후 DTO로 반환
        return resorts.stream()
                .filter(r -> r.getLat() != null && r.getLng() != null)
                .map(ResortDto::from)
                .toList();
    }
    //단이 리조트 기본 정보 조회
    @Override
    public ResortDto getResort(Long id) {
        SkiResort r = repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Resort not found: " + id));
        // 필요 필드만 선택적으로 DTO구성
        return new ResortDto(
            r.getId(), r.getName(), r.getDescription(),
            r.getLat(), r.getLng()
        );
    }
    // ㅣㄹ조트 상세 정보 조회
    @Override
    public ResortDetailDto getDetail(Long id) {
        SkiResort r = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Resort not found: " + id));
        return ResortDetailDto.from(r);
    }
}
