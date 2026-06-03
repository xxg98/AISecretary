package com.kailei.aisecretary.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.kailei.aisecretary.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/game")
@Tag(name = "游戏接口")
public class GameController {

    @SaCheckRole("user")
    @Operation(summary = "开始游戏")
    @PostMapping("/startGame")
    public Result startGame(){ // 开始游戏
        return Result.success("开始成功");
    }

    @SaCheckRole("operation")
    @Operation(summary = "充值")
    @PostMapping("/topUp")
    public Result topUp(){ //充值
        return Result.success("充值成功");
    }

    @SaCheckRole("admin")
    @Operation(summary = "封号")
    @PostMapping("/accountSuspension")
    public Result accountSuspension(){ //封号
        return Result.success("封号成功");
    }


}
