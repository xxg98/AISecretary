package com.kailei.aisecretary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
public class ScanDto {

    @NotNull(message = "状态码不能为空")
    @Schema(description = "状态码",nullable = true,requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "用户id")
    private String userId;
}
