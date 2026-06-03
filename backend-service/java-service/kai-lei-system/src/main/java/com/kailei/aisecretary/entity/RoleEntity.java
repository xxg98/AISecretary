package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@AutoTable(value = "kl_role", comment = "角色表")
@TableName("kl_role")
public class RoleEntity extends BaseEntity {
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("角色名称")
    private String name;

    @ColumnComment("角色标识")
    private String roleKey;

    @ColumnComment("角色说明")
    private String description;

    @ColumnComment("角色状态：0禁用 1启用")
    private Integer status;
}
