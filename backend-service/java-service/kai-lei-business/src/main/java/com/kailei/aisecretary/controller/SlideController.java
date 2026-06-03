package com.kailei.aisecretary.controller;

import com.kailei.aisecretary.service.ISlideService;
import com.kailei.aisecretary.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/slide")
@Tag(name = "轮播图接口")
public class SlideController {

    @Autowired
    private ISlideService iSlideService;

    @Operation(summary = "获取轮播图")
    @GetMapping("/getBanners")
    public Result getBanners(){
        return Result.success(iSlideService.getBanners());
    }
}