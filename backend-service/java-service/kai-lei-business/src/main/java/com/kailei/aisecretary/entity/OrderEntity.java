package com.kailei.aisecretary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.PrimaryKey;

@Data
@TableName("kl_order")
@AutoTable(value = "kl_order", comment = "订单表")
public class OrderEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    @PrimaryKey(autoIncrement = true)
    private Long id;

    @ColumnComment("产品ID")
    private Long productId;

    @ColumnComment("用户ID")
    private Long userId;

    @ColumnComment("订单状态：0待处理 1处理中 2完成 3取消")
    private Integer orderStatus;
}
