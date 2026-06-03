package com.kailei.aisecretary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 数据库表字段一致
 */
@Data
public class UsersRegisterDto {

    /***
     * TODO SJY 敏感词要讲
     */
    @NotNull(message = "账号不能为空")
    @Size(min = 4,max = 10,message = "账号必须在4-10位")
    @Pattern(
            regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$",
            message = "账号只能包含数字、字母和中文，不能有其他特殊字符"
    )
    @Schema(description = "账号",defaultValue = "zhangsan",nullable = true,requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;



    @Size(min = 4,max = 10,message = "密码必须在4-10位")
    // 规则1：必须包含大小写字母 + 数字
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
            message = "密码必须包含大小写字母和数字"
    )
    // 规则2：禁止连续重复数字（如666）、连续递增/递减数字（如123/987）
    @Pattern(
            regexp = "^(?!.*(\\d)\\1\\1)(?!.*(012|123|234|345|456|567|678|789|987|876|765|654|543|432|321|210)).*$",
            message = "不能有连续的数字如666或123"
    )
    @Schema(description = "密码",defaultValue = "147258Abc.",nullable = true,requiredMode = Schema.RequiredMode.REQUIRED)
    private String pass;


    @NotNull(message = "昵称不能为空")
    @Size(min = 3,max = 10,message = "昵称必须在4-10位")
    @Schema(description = "昵称",defaultValue = "石老师",nullable = true,requiredMode = Schema.RequiredMode.REQUIRED)
    private String nickname;
}
