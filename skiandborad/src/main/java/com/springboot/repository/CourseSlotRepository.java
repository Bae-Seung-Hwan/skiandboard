package com.springboot.repository;
import com.springboot.domain.CourseSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface CourseSlotRepository extends JpaRepository<CourseSlot, Long> {
    List<CourseSlot> findTop4ByOrderByDisplayOrderAsc();
}