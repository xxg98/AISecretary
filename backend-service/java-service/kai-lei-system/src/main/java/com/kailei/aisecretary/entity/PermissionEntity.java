package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@AutoTable(value = "kl_permission", comment = "权限表")
@TableName("kl_permission")
public class PermissionEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("权限名称")
    private String name;

    @ColumnComment("权限标识")
    private String permissionKey;

    @ColumnComment("权限类型：menu菜单 button按钮 api接口")
    private String permissionType;

    @ColumnComment("权限资源路径")
    private String resourcePath;

    @ColumnComment("权限说明")
    private String description;
}
