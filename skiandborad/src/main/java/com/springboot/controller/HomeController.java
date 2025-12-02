package com.springboot.controller;
import com.springboot.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
	private final HomeService homeService;

    @GetMapping("/")// index.html로 전달
    public String index(Model model) {
        model.addAttribute("resorts", homeService.getFeaturedResorts());
        model.addAttribute("slots", homeService.getCourseSlots());
        return "index";
    }
}
