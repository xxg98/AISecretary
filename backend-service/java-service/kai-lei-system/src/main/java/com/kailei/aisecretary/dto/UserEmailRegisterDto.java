package com.kailei.aisecretary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 邮箱注册DTO
 */
@Data
public class UserEmailRegisterDto {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", defaultValue = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", defaultValue = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}