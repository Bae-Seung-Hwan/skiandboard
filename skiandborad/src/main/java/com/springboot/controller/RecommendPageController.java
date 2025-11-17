package com.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecommendPageController {

    /** 추천 페이지 뷰 (templates/recommend.html) */
    @GetMapping("/recommend")
    public String page() {
        return "recommend";
    }
}
