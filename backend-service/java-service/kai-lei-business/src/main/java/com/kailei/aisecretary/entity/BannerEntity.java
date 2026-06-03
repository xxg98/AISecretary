package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@TableName("kl_banner")
@AutoTable(value = "kl_banner", comment = "轮播图表")
public class BannerEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("轮播图图片地址")
    private String imgUrl;

    @ColumnComment("点击跳转地址")
    private String linkUrl;

    @ColumnComment("备注信息")
    private String remark;

    @ColumnComment("状态：0禁用 1启用")
    private Integer status;
}
