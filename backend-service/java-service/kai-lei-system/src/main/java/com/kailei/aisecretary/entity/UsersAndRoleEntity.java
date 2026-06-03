package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@AutoTable(value = "kl_user_role", comment = "用户角色关联表")
@TableName("kl_user_role")
public class UsersAndRoleEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("用户ID")
    private Long userId;

    @ColumnComment("角色ID")
    private Long roleId;
}
