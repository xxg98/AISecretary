package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@AutoTable(value = "kl_role_permission", comment = "角色权限关联表")
@TableName("kl_role_permission")
public class RoleAndPermissionEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("角色ID")
    private Long roleId;

    @ColumnComment("权限ID")
    private Long permissionId;
}
