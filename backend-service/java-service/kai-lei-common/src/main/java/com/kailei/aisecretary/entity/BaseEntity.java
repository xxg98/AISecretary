package com.kailei.aisecretary.entity;

import lombok.Data;
import org.dromara.autotable.annotation.ColumnComment;

import java.util.Date;

/**
 * 公共复用的对象属性
 */
@Data
public class BaseEntity {
    @ColumnComment("创建人")
    protected String createBy;
    @ColumnComment("创建时间")
    protected Date createTime;
    @ColumnComment("更新人")
    protected String updateBy;
    @ColumnComment("更新时间")
    protected Date updateTime;

    //有效字段
    @ColumnComment("有效字段")
    protected Integer isDelete;


}
