package com.springboot.controller;

import com.springboot.dto.ResortDetailDto;
import com.springboot.service.SkiResortService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/resorts")
public class ResortController {

    private final SkiResortService skiResortService;

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        ResortDetailDto resort = skiResortService.getDetail(id);
        model.addAttribute("resort", resort);
        return "resort";
    }
}
