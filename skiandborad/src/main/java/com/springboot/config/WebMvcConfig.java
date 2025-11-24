package com.springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 브라우저에서 /uploads/** 으로 접근하면
        // 실제 로컬 디렉터리 uploadDir 로 연결
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // 선택: 1시간 캐싱

        // 혹시 이미지 외의 정적 폴더 등을 추가하고 싶을 때 사용
        // registry.addResourceHandler("/static/**")
        //         .addResourceLocations("classpath:/static/");
    }
}
