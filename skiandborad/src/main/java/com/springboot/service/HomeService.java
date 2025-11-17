package com.springboot.service;
import com.springboot.dto.CourseSlotDto;
import com.springboot.dto.MainResortCardDto;

import java.util.List;
public interface HomeService {
	List<MainResortCardDto> getFeaturedResorts(); // 메인 카드
    List<CourseSlotDto> getCourseSlots();         // 시간표 2x2
}
