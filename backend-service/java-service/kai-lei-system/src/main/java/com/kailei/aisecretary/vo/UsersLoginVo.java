package com.kailei.aisecretary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库表字段一致
 */
@Data
public class UsersLoginVo {
    @Schema(description = "昵称")
    private String nickname;
    @Schema(description = "token")
    private String token;
}
