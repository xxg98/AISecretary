package com.kailei.aisecretary.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库表字段一致
 */
@Data
public class UsersVo {
    @Schema(description = "用户id")
    private Integer id;
    @Schema(description = "用户名")
    private String name;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "年龄")
    private String age;
}
