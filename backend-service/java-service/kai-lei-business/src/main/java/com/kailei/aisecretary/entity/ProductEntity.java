package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

import java.math.BigDecimal;

@Data
@TableName("kl_product")
@AutoTable(value = "kl_product", comment = "产品表")
public class ProductEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("产品名称")
    private String productName;

    @ColumnComment("价格")
    private BigDecimal price;

    @ColumnComment("库存")
    private Integer stock;

    @ColumnComment("产品类型")
    private String type;

    @ColumnComment("产品状态：0下架 1上架")
    private Integer status;
}
