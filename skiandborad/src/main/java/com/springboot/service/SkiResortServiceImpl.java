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

    private final SkiResortRepository repo;

    @Override
    public List<ResortDto> list(ResortType type) {
        List<SkiResort> resorts =
                (type == null) ? repo.findAll() : repo.findByType(type);

        return resorts.stream()
                .filter(r -> r.getLat() != null && r.getLng() != null)
                .map(ResortDto::from)
                .toList();
    }
    @Override
    public ResortDto getResort(Long id) {
        SkiResort r = repo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Resort not found: " + id));
        return new ResortDto(
            r.getId(), r.getName(), r.getDescription(),
            r.getLat(), r.getLng()
        );
    }
    @Override
    public ResortDetailDto getDetail(Long id) {
        SkiResort r = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Resort not found: " + id));
        return ResortDetailDto.from(r);
    }
}
