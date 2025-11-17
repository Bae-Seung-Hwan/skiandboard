package com.springboot.controller;

import com.springboot.domain.ResortType;
import com.springboot.dto.ResortDto;
import com.springboot.service.SkiResortService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resorts")
@RequiredArgsConstructor
public class ResortApiController {

    private final SkiResortService resortService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResortDto> list(@RequestParam(required = false) ResortType type) {
        return resortService.list(type);
    }
}
