package com.springboot.service;

import com.springboot.domain.ResortType;
import com.springboot.dto.ResortDetailDto;
import com.springboot.dto.ResortDto;

import java.util.List;

public interface SkiResortService {
    List<ResortDto> list(ResortType type);
    ResortDto getResort(Long id);
    ResortDetailDto getDetail(Long id);
}
