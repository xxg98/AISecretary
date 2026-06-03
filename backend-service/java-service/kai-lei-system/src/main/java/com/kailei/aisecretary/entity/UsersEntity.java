package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

/**
 * 系统用户表，对应每个使用 AI 秘书的人。
 */
@Data
@AutoTable(value = "kl_user", comment = "用户表")
@TableName("kl_user")
public class UsersEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("登录用户名")
    private String name;

    @ColumnComment("用户昵称")
    private String nickname;

    @ColumnComment("登录密码密文")
    private String pass;

    @ColumnComment("密码盐值")
    private String saltValue;

    @ColumnComment("邮箱")
    private String email;

    @ColumnComment("手机号")
    private String mobile;

    @ColumnComment("头像地址")
    private String avatarUrl;

    @ColumnComment("账号状态：0禁用 1正常")
    private Integer status;

    @ColumnComment("用户默认 AI 秘书ID")
    private Long defaultSecretaryId;
}
