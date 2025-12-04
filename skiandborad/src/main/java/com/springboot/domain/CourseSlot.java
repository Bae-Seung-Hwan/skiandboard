package com.springboot.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseSlot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;      
    private String timeRange;  
    private String note;
    private Integer displayOrder;
}