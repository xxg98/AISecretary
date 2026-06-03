package com.kailei.aisecretary.controller;

import com.kailei.aisecretary.dto.ScanDto;
import com.kailei.aisecretary.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/scan")
@Tag(name = "扫一扫功能")
public class ScanController {

    /**
     * 101 扫码支付
     */
    @Operation(summary = "扫一扫")
    @PostMapping("/scan")
    public Result scan(@Valid @RequestBody ScanDto scanDto){

        // 处理扫码逻辑
        log.info("扫码成功{}",scanDto.toString());

        return Result.success();
    }


}
