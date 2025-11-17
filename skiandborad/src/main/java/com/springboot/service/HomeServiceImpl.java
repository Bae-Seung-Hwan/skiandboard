package com.springboot.service;

import java.util.List;
import com.springboot.domain.SkiResort;
import com.springboot.dto.CourseSlotDto;
import com.springboot.dto.MainResortCardDto;
import com.springboot.repository.CourseSlotRepository;
import com.springboot.repository.SkiResortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
	private final SkiResortRepository skiResortRepository;
	private final CourseSlotRepository courseSlotRepository;
	@Override
	public List<MainResortCardDto> getFeaturedResorts() {
		return skiResortRepository.findTop6ByOrderByIdDesc()
                .stream()
                .map(this::toCardDto)
                .toList();
	}

	@Override
	public List<CourseSlotDto> getCourseSlots() {
		return courseSlotRepository.findTop4ByOrderByDisplayOrderAsc()
        .stream()
        .map(cs -> new CourseSlotDto(cs.getTitle(), cs.getTimeRange(), cs.getNote()))
        .toList();
	}
	private MainResortCardDto toCardDto(SkiResort r) {
        return new MainResortCardDto(r.getId(), r.getName(), r.getDescription(),
                r.getHeroImageUrl(), r.getLat(), r.getLng());
    }

}
